package top.smscloudapp.basic.student

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // ✅ Change only if your domain/path changes
    private val BASE_URL = "https://students.basic.smscloudapp.top"
    private val AFTER_LOGIN_PATH = "/student-portal"

    // Avoid sending multiple times
    private var tokenSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        webView = findViewById(R.id.webView)

        // Cookies required for session login in WebView
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false

                if (view != null && url != null) {
                    // ✅ After user reaches post-login page, send token once
                    if (!tokenSent && url.contains(AFTER_LOGIN_PATH)) {
                        tokenSent = true
                        sendFcmTokenToLaravel(view)
                    }
                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        // FILE UPLOAD SUPPORT
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val intent = fileChooserParams?.createIntent()
                startActivityForResult(intent!!, 100)
                return true
            }
        }

        // FILE DOWNLOAD SUPPORT
        webView.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(
                url: String?,
                userAgent: String?,
                contentDisposition: String?,
                mimeType: String?,
                contentLength: Long
            ) {
                if (url == null) return
                if (url.startsWith("blob:")) return

                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimeType)

                val cookies = CookieManager.getInstance().getCookie(url)
                if (!cookies.isNullOrEmpty()) {
                    request.addRequestHeader("Cookie", cookies)
                }

                request.addRequestHeader("User-Agent", userAgent ?: "")
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                request.setDescription("Downloading file...")
                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )

                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimeType)
                )

                val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
            }
        })

        // Rotation safe load/restore
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(BASE_URL)
        }
    }

    /**
     * ✅ Sends FCM token to Laravel web route using current WebView session cookie.
     * Works with Laravel 11 CSRF by reading <meta name="csrf-token"> from the page.
     *
     * Laravel route must exist: POST /push/register (middleware: auth)
     */
    private fun sendFcmTokenToLaravel(webView: WebView) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isNullOrBlank()) return@addOnSuccessListener

            val js = """
                (function() {
                    try {
                        var meta = document.querySelector('meta[name="csrf-token"]');
                        var csrf = meta ? meta.getAttribute('content') : null;

                        fetch('$BASE_URL/push/register', {
                          method: 'POST',
                          headers: Object.assign({
                            'Content-Type': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest'
                          }, csrf ? {'X-CSRF-TOKEN': csrf} : {}),
                          body: JSON.stringify({ device_token: '$token' }),
                          credentials: 'include'
                        })
                        .then(function(r){ return r.json().catch(function(){ return {}; }); })
                        .then(function(d){ console.log('push saved', d); })
                        .catch(function(e){ console.log('push error', e); });
                    } catch (e) {
                        console.log('push exception', e);
                    }
                })();
            """.trimIndent()

            webView.evaluateJavascript(js, null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100) {
            filePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            )
            filePathCallback = null
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
