package su.tagir.apps.radiot.model.api


import okhttp3.ResponseBody
import retrofit2.http.*
import su.tagir.apps.radiot.model.entries.GitterMessage
import su.tagir.apps.radiot.model.entries.Token


interface GitterAuthClient {

    @FormUrlEncoded
    @POST("login/oauth/token")
    @Headers("Accept: application/json")
    suspend fun auth(
            @Field("client_id") appId: String?,
            @Field("client_secret") appKey: String?,
            @Field("code") code: String?,
            @Field("redirect_uri") redirectUri: String?,
            @Field("grant_type") type: String = "authorization_code"): Token

}

interface GitterClient {


    @GET("v1/rooms/{roomId}/chatMessages")
    suspend fun getRoomMessages(@Path("roomId") roomId: String,
                        @Query("limit") limit: Int?,
                        @Query("beforeId") before: String? = null): List<GitterMessage>

    @GET("v1/rooms/{roomId}/chatMessages/{messageId}")
    suspend fun getRoomMessageById(@Path("roomId") roomId: String,
                           @Path("messageId") messageId: String): GitterMessage

    @FormUrlEncoded
    @POST("v1/rooms/{roomId}/chatMessages")
    suspend fun sendMessage(@Path("roomId") roomId: String,
                    @Field("text") text: String)

    @FormUrlEncoded
    @PUT("v1/rooms/{roomId}/chatMessages/{chatMessageId}")
    suspend fun updateMessage(@Path("roomId") roomId: String,
                      @Path("chatMessageId") chatMessageId: String,
                      @Field("text") text: String): GitterMessage

}

interface GitterStreamClient {

    @Streaming
    @GET("v1/rooms/{roomId}/chatMessages")
    suspend fun getRoomMessagesStream(@Path("roomId") roomId: String): ResponseBody

    @Streaming
    @GET("v1/rooms/{roomId}/events")
    suspend fun getRoomEventsStream(@Path("roomId") roomId: String): ResponseBody
}