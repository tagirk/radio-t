package su.tagir.apps.radiot.model.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import su.tagir.apps.radiot.model.api.NewsRestClient
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.articleMapper
import su.tagir.apps.radiot.model.entries.insert


class NewsRepositoryImpl(private val newsRestClient: NewsRestClient,
                         private val database: RadiotDb) : NewsRepository {

    override fun getArticles(): Flow<List<Article>> =
            database.articleQueries.findByDeletedAndArchivedStates(deleted = false, archived = false, mapper = articleMapper).asFlow().mapToList()

    override suspend fun updateActiveArticle() {
        val article = newsRestClient.getActiveArticle()
        article.insert(database.articleQueries)
    }

    override suspend fun updateArticles(){
        val articles = newsRestClient.getLastArticles(50)
        database.transaction {
            articles.forEach { a -> a.insert(database.articleQueries) }
        }
    }

}