package su.tagir.apps.radiot.model.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.invoke
import su.tagir.apps.radiot.model.api.NewsRestClient
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.articleMapper
import su.tagir.apps.radiot.model.entries.insert


class NewsRepositoryImpl(private val newsRestClient: NewsRestClient,
                         private val database: RadiotDb,
                         private val dispatcher: CoroutineDispatcher = Dispatchers.Default) : NewsRepository {

    override fun getArticles(): Flow<List<Article>> =
            database.articleQueries.findByDeletedAndArchivedStates(deleted = false, archived = false, mapper = articleMapper)
                    .asFlow()
                    .mapToList(dispatcher)

    @ExperimentalCoroutinesApi
    override suspend fun updateActiveArticle() {
        val article = newsRestClient.getActiveArticle()
        dispatcher {
            article.insert(database.articleQueries)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun updateArticles(){
        val articles = newsRestClient.getLastArticles(50)
        dispatcher {
            database.transaction {
                articles.forEach { a -> a.insert(database.articleQueries) }
            }
        }
    }

}