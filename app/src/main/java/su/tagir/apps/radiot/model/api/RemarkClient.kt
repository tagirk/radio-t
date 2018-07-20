package su.tagir.apps.radiot.model.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import su.tagir.apps.radiot.model.entries.Comment
import su.tagir.apps.radiot.model.entries.CommentsTree
import su.tagir.apps.radiot.model.entries.PostInfo

interface RemarkClient {

    @POST("comment")
    fun postComment(@Body comment: Comment): Single<Comment>

    @POST("preview")
    fun previewComment(@Body comment: Comment): Single<Comment>

    @GET("find")
    fun getComments(@Query("site") siteId: String = "radiot",
                    @Query("url") postUrl: String,
                    @Query("sort") sort:String = "-time",
                    @Query("format") format: String = "tree"): Single<CommentsTree>

    @POST("counts")
    fun getCommentsCount(@Query("site") siteId: String = "radiot",
                         @Body urls: List<String>): Single<List<PostInfo>>
}