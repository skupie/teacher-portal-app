package top.smscloudapp.basic.student.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterDeviceRequest(
    @SerializedName("device_token")
    val deviceToken: String,
    @SerializedName("platform")
    val platform: String = "android",
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("app_version")
    val appVersion: String
)
