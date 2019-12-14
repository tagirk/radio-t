package su.tagir.apps.radiot.model.repository

import kotlinx.coroutines.flow.Flow
import su.tagir.apps.radiot.model.entries.Article

interface NewsRepository {

    fun getArticles(): Flow<List<Article>>

    suspend fun updateActiveArticle()

    suspend fun updateArticles()
}