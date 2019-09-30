package su.tagir.apps.radiot.model.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import su.tagir.apps.radiot.model.entries.Comment
import su.tagir.apps.radiot.model.entries.CommentsTree
import su.tagir.apps.radiot.model.entries.PostInfo

interface RemarkClient {

    @POST("comment")
    suspend fun postComment(@Body comment: Comment): Comment

    @POST("preview")
    suspend fun previewComment(@Body comment: Comment): Comment

    @GET("find")
    suspend fun getComments(@Query("site") siteId: String = "radiot",
                    @Query("url") postUrl: String,
                    @Query("sort") sort:String = "-time",
                    @Query("format") format: String = "tree"): CommentsTree

    @POST("counts")
    suspend fun getCommentsCount(@Query("site") siteId: String = "radiot",
                         @Body urls: List<String>): List<PostInfo>
}