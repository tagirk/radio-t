package su.tagir.apps.radiot.model.api


import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*
import su.tagir.apps.radiot.model.entries.GitterMessage
import su.tagir.apps.radiot.model.entries.Token


interface GitterAuthClient {

    @FormUrlEncoded
    @POST("login/oauth/token")
    @Headers("Accept: application/json")
    fun auth(
            @Field("client_id") appId: String?,
            @Field("client_secret") appKey: String?,
            @Field("code") code: String?,
            @Field("redirect_uri") redirectUri: String?,
            @Field("grant_type") type: String = "authorization_code"): Single<Token>

}

interface GitterClient {


    @GET("v1/rooms/{roomId}/chatMessages")
    fun getRoomMessages(@Path("roomId") roomId: String,
                        @Query("limit") limit: Int?,
                        @Query("beforeId") before: String?): Single<List<GitterMessage>>

    @GET("v1/rooms/{roomId}/chatMessages/{messageId}")
    fun getRoomMessageById(@Path("roomId") roomId: String,
                           @Path("messageId") messageId: String): Single<GitterMessage>

    @FormUrlEncoded
    @POST("v1/rooms/{roomId}/chatMessages")
    fun sendMessage(@Path("roomId") roomId: String,
                    @Field("text") text: String): Single<GitterMessage>

    @FormUrlEncoded
    @PUT("v1/rooms/{roomId}/chatMessages/{chatMessageId}")
    fun updateMessage(@Path("roomId") roomId: String,
                      @Path("chatMessageId") chatMessageId: String,
                      @Field("text") text: String): Single<GitterMessage>

}

interface GitterStreamClient {

    @Streaming
    @GET("v1/rooms/{roomId}/chatMessages")
    fun getRoomMessagesStream(@Path("roomId") roomId: String): Flowable<ResponseBody>

    @Streaming
    @GET("v1/rooms/{roomId}/events")
    fun getRoomEventsStream(@Path("roomId") roomId: String): Flowable<ResponseBody>
}