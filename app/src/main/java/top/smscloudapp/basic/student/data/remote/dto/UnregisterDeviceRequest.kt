package top.smscloud.basic.student.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UnregisterDeviceRequest(
    @SerializedName("device_token")
    val deviceToken: String
)
