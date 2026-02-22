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
    suspend fun logout(@Header("Authorization") bearer: String): ApiMessage
}
