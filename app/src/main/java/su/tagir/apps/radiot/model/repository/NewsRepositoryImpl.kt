package su.tagir.apps.radiot.model.repository

import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import su.tagir.apps.radiot.model.api.NewsRestClient
import su.tagir.apps.radiot.model.db.NewsDao
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.repository.EntryRepositoryImpl.Companion.PAGE_SIZE
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider

class NewsRepositoryImpl(private val newsRestClient: NewsRestClient,
                         private val newsDao: NewsDao,
                         private val scheduler: BaseSchedulerProvider) : NewsRepository {

    override fun getArticles(): Flowable<out List<Article>> = RxPagedListBuilder(newsDao.getArticles(), PAGE_SIZE)
            .setFetchScheduler(scheduler.io())
            .setNotifyScheduler(scheduler.ui())
            .buildFlowable(BackpressureStrategy.BUFFER)

    override fun updateActiveArticle(): Single<Article> = newsRestClient
            .getActiveArticle()
            .doOnSuccess { newsDao.updateActiveArticle(it) }

    override fun updateArticles(): Single<List<Article>> = newsRestClient
            .getLastArticles(20)
            .doOnSuccess { newsDao.updateArticles(it) }

}