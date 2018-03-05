package su.tagir.apps.radiot.model.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import su.tagir.apps.radiot.model.entries.RTEntry


interface RestClient {

    @GET("last/{posts}")
    fun getPosts(@Path("posts") posts: Int,
                 @Query("categories") vararg categories: String): Single<List<RTEntry>>

    @GET("search")
    fun search(@Query("q") query: String,
               @Query("skip") skip: Int?,
               @Query("limit") limit: Int?): Single<List<RTEntry>>
}