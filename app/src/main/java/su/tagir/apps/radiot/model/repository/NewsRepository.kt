package su.tagir.apps.radiot.model.repository

import io.reactivex.Single
import su.tagir.apps.radiot.model.Prefs
import su.tagir.apps.radiot.model.api.NewsRestClient
import su.tagir.apps.radiot.model.db.NewsDao
import su.tagir.apps.radiot.model.entries.Article
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(private val newsRestClient: NewsRestClient,
                                         private val newsDao: NewsDao,
                                         prefs: Prefs) {

    fun getArticles() = newsDao.getArticles()

    fun updateActiveArticle():Single<Article> = newsRestClient
            .getActiveArticle()
            .doOnSuccess { newsDao.updateActiveArticle(it) }

    fun updateArticles(): Single<List<Article>> = newsRestClient
            .getLastArticles(20)
            .doOnSuccess{newsDao.updateArticles(it)}

    var showTimer = prefs.showTimer
}