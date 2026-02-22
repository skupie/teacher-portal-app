package top.smscloudapp.basic.student.push

import top.smscloudapp.basic.student.BuildConfig
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import top.smscloudapp.basic.student.data.remote.ApiClient
import top.smscloudapp.basic.student.data.remote.dto.RegisterDeviceRequest
import top.smscloudapp.basic.student.data.session.SessionManager

class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val session = SessionManager(applicationContext)
        session.saveFcmToken(token)

        val authToken = session.getAuthToken() ?: return

        scope.launch {
            runCatching {
                ApiClient.smsApi.registerDevice(
                    bearer = "Bearer $authToken",
                    body = RegisterDeviceRequest(
                        deviceToken = token,
                        deviceName = Build.MODEL ?: "Android",
                        appVersion = BuildConfig.VERSION_NAME
                    )
                )
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Optional: show custom local notification here.
    }
}
