package top.smscloudapp.basic.student.data.remote

import top.smscloudapp.basic.student.data.remote.dto.ApiMessage
import top.smscloudapp.basic.student.data.remote.dto.LoginRequest
import top.smscloudapp.basic.student.data.remote.dto.LoginResponse
import top.smscloudapp.basic.student.data.remote.dto.RegisterDeviceRequest
import top.smscloudapp.basic.student.data.remote.dto.UnregisterDeviceRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HTTP
import retrofit2.http.POST

interface SmsApi {

    @POST("api/mobile/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/mobile/push/devices")
    suspend fun registerDevice(
        @Header("Authorization") bearer: String,
        @Body body: RegisterDeviceRequest
    ): ApiMessage

    @HTTP(method = "DELETE", path = "api/mobile/push/devices", hasBody = true)
    suspend fun unregisterDevice(
        @Header("Authorization") bearer: String,
        @Body body: UnregisterDeviceRequest
    ): ApiMessage

    @POST("api/mobile/logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    ): ApiMessage
}
