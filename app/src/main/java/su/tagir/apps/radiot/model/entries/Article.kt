package su.tagir.apps.radiot.model.entries

import com.google.gson.annotations.SerializedName
import su.tagir.apps.radiot.model.entities.ArticleQueries
import java.util.*

data class Article(
        @SerializedName("slug") val slug: String,
        @SerializedName("title") val title: String? = null,
        @SerializedName("content") val content: String? = null,
        @SerializedName("snippet") val snippet: String? = null,
        @SerializedName("pic") val image: String? = null,
        @SerializedName("link") val link: String? = null,
        @SerializedName("author") val author: String? = null,
        @SerializedName("ts") val date: Date? = null,
        @SerializedName("ats") val addedDate: Date? = null,
        @SerializedName("active") val active: Boolean = false,
        @SerializedName("geek") val geek: Boolean = false,
        @SerializedName("domain") val domain: String? = null,
        @SerializedName("comments") val comments: Int = 0,
        @SerializedName("likes") val likes: Int = 0,
        @SerializedName("del") val deleted: Boolean = false,
        @SerializedName("archived") val archived: Boolean = false
)

fun Article.insert(articleQueries: ArticleQueries) {
    articleQueries.insert(slug, title, content, snippet, image, link, author, date, addedDate, active, geek, domain, comments, likes, deleted, archived)
}

val articleMapper: (slug: String,
                    title: String?,
                    content: String?,
                    snippet: String?,
                    image: String?,
                    link: String?,
                    author: String?,
                    date: Date?,
                    addedDate: Date?,
                    active: Boolean,
                    geek: Boolean,
                    domain: String?,
                    comments: Int,
                    likes: Int,
                    deleted: Boolean,
                    archived: Boolean) -> Article
    get() = { slug, title, content, snippet, image, link, author, date, addedDate, active, geek, domain, comments, likes, deleted, archived ->
        Article(slug, title, content, snippet, image, link, author, date, addedDate, active, geek, domain, comments, likes, deleted, archived)
    }