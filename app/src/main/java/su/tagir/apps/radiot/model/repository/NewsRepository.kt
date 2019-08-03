package su.tagir.apps.radiot.model.repository

import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Single
import su.tagir.apps.radiot.model.Prefs
import su.tagir.apps.radiot.model.api.NewsRestClient
import su.tagir.apps.radiot.model.db.NewsDao
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.EntryRepository.Companion.PAGE_SIZE
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(private val newsRestClient: NewsRestClient,
                                         private val newsDao: NewsDao,
                                         private val scheduler: BaseSchedulerProvider,
                                         prefs: Prefs) {

    fun getArticles() = RxPagedListBuilder(newsDao.getArticles(), PAGE_SIZE)
            .setFetchScheduler(scheduler.io())
            .setNotifyScheduler(scheduler.ui())
            .buildFlowable(BackpressureStrategy.BUFFER)

    fun updateActiveArticle():Single<Article> = newsRestClient
            .getActiveArticle()
            .doOnSuccess { newsDao.updateActiveArticle(it) }

    fun updateArticles(): Single<List<Article>> = newsRestClient
            .getLastArticles(20)
            .doOnSuccess{newsDao.updateArticles(it)}

    var showTimer = prefs.showTimer
}