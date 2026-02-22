package top.smscloudapp.basic.student.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("login")
    val login: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("role")
    val role: String, // "teacher" or "student"
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("device_token")
    val deviceToken: String?,
    @SerializedName("platform")
    val platform: String = "android",
    @SerializedName("app_version")
    val appVersion: String
)
