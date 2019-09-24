package su.tagir.apps.radiot.model.repository

import io.reactivex.Flowable
import io.reactivex.Single
import su.tagir.apps.radiot.model.entries.Article

interface NewsRepository {

    fun getArticles(): Flowable<out List<Article>>

    fun updateActiveArticle(): Single<Article>

    fun updateArticles(): Single<List<Article>>
}