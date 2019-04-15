package su.tagir.apps.radiot.model.db

import androidx.paging.DataSource
import androidx.room.*
import su.tagir.apps.radiot.model.entries.Article

@Dao
abstract class NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertArticle(article: Article)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertArticles(articles: List<Article>)

    @Query("DELETE FROM article")
    abstract fun deleteArticles()

    @Query("UPDATE article SET active = 0 WHERE active = 1")
    abstract fun resetActiveArticle()

    @Query("SELECT * FROM article WHERE deleted == 0 AND archived == 0 ORDER BY active DESC, addedDate DESC")
    abstract fun getArticles(): DataSource.Factory<Int, Article>

    @Transaction
    open fun updateArticles(articles: List<Article>) {
        deleteArticles()
        insertArticles(articles)
    }

    @Transaction
    open fun updateActiveArticle(article: Article) {
        resetActiveArticle()
        insertArticle(article)
    }
}