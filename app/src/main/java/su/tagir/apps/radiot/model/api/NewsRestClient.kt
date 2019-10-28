package su.tagir.apps.radiot.model.api

import retrofit2.http.GET
import retrofit2.http.Path
import su.tagir.apps.radiot.model.entries.Article

interface NewsRestClient {

    @GET("news/last/{count}")
    suspend fun getLastArticles(@Path("count")count: Int): List<Article>

    @GET("news/slug/{slug}")
    suspend fun getArticle(@Path("slug") slug: String): Article

    @GET("news/active")
    suspend fun getActiveArticle():Article


}
