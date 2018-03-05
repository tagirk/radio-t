package su.tagir.apps.radiot.model.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import su.tagir.apps.radiot.model.entries.Article

interface NewsRestClient {

    @GET("news/last/{count}")
    fun getLastArticles(@Path("count")count: Int): Single<List<Article>>

    @GET("news/slug/{slug}")
    fun getArticle(@Path("slug") slug: String): Single<Article>

    @GET("news/active")
    fun getActiveArticle(): Single<Article>
}
