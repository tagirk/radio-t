package su.tagir.apps.radiot.model.entries

import android.content.ContentValues
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.room.*
import com.google.gson.annotations.SerializedName
import su.tagir.apps.radiot.model.db.StringListConverter
import su.tagir.apps.radiot.model.entries.TimeLabel.Companion.PODCAST_TIME
import su.tagir.apps.radiot.model.entries.TimeLabel.Companion.TABLE_NAME
import su.tagir.apps.radiot.model.entries.TimeLabel.Companion.TOPIC
import java.util.*
import kotlin.collections.ArrayList

object EntryState {
    const val PLAYING = 1
    const val PAUSED = 2
    const val IDLE = 0
}

@Entity(tableName = Entry.TABLE_NAME)
data class Entry(
        @PrimaryKey(autoGenerate = false)
        val url: String,
        val title: String? = null,
        val date: Date? = null,
        val categories: List<String>? = null,
        val image: String? = null,
        val fileName: String? = null,
        val body: String? = null,
        val showNotes: String? = null,
        val audioUrl: String? = null,
        val progress: Long = 0L,
        val state: Int = EntryState.IDLE,
        val file: String? = null,
        val downloadId: Long = -1L,
        val downloadProgress: Int = -1,
        val commentsCount: Int = 0) {

    @Ignore
    constructor(cv: ContentValues) :
            this(url = cv.getAsString("url"),
                    title = cv.getAsString("title"),
                    date = Date(cv.getAsLong("date")),
                    categories = TextUtils.split(",", cv.getAsString("categories")).asList(),
                    image = cv.getAsString("image"),
                    fileName = cv.getAsString("fileName"),
                    body = cv.getAsString("body"),
                    showNotes = cv.getAsString("showNotes"),
                    audioUrl = cv.getAsString("audioUrl"),
                    progress = cv.getAsLong(Entry.PROGRESS),
                    state = cv.getAsInteger("state") ?: EntryState.IDLE,
                    file = cv.getAsString("file") ?: null,
                    downloadProgress = cv.getAsInteger(Entry.DOWNLOAD_PROGRESS),
                    commentsCount = cv.getAsInteger("commentsCount"))

    @Ignore
    constructor(c: Cursor) :
            this(url = c.getString(c.getColumnIndexOrThrow("url")),
                    title = c.getString(c.getColumnIndexOrThrow("title")),
                    date = Date(c.getLong(c.getColumnIndexOrThrow("date"))),
                    categories = TextUtils.split(",", c.getString(c.getColumnIndexOrThrow("categories"))).asList(),
                    image = c.getString(c.getColumnIndexOrThrow("image")),
                    fileName = c.getString(c.getColumnIndexOrThrow("fileName")),
                    body = c.getString(c.getColumnIndexOrThrow("body")),
                    showNotes = c.getString(c.getColumnIndexOrThrow("showNotes")),
                    audioUrl = c.getString(c.getColumnIndexOrThrow("audioUrl")),
                    progress = c.getLong(c.getColumnIndexOrThrow(Entry.PROGRESS)),
                    state = c.getInt(c.getColumnIndexOrThrow("state")),
                    file = c.getString(c.getColumnIndexOrThrow("file")),
                    downloadProgress = c.getInt(c.getColumnIndexOrThrow(Entry.DOWNLOAD_PROGRESS)),
                    commentsCount = c.getInt(c.getColumnIndexOrThrow("commentsCount")))

    @Ignore
    constructor(rtEntry: RTEntry) :
            this(url = rtEntry.url,
                    title = rtEntry.title,
                    date = rtEntry.date,
                    categories = rtEntry.categories,
                    image = rtEntry.image,
                    fileName = rtEntry.fileName,
                    body = rtEntry.body,
                    showNotes = rtEntry.showNotes,
                    audioUrl = rtEntry.audioUrl,
                    progress = 0L,
                    state = EntryState.IDLE,
                    file = null,
                    downloadId = -1,
                    downloadProgress = -1,
                    commentsCount =  0)

    val chatUrl
        get() = "https://chat.radio-t.com/logs/radio-t-${title?.substring(8)}.html"

    companion object {
        const val TABLE_NAME = "entry"
        const val URL = "url"
        const val TITLE = "title"
        const val DATE = "date"
        const val CATEGORIES = "categories"
        const val IMAGE = "image"
        const val FILE_NAME = "fileName"
        const val BODY = "body"
        const val SHOWNOTES = "showNotes"
        const val AUDIO_URl = "audioUrl"
        const val PROGRESS = "progress"
        const val STATE = "state"
        const val FILE = "file"
        const val DOWNLOAD_ID = "downloadId"
        const val DOWNLOAD_PROGRESS = "downloadProgress"
    }
}

@Entity(tableName = TABLE_NAME,
        primaryKeys = [TOPIC, PODCAST_TIME])
@TypeConverters(StringListConverter::class)
data class TimeLabel(
        val topic: String,
        val time: Long?,
        val duration: Long?,

        @ColumnInfo(name = PODCAST_TIME)
        val podcastTime: Date) {

    companion object {
        const val TABLE_NAME = "time_label"
        const val TOPIC = "topic"
        const val TIME = "time"
        const val DURATION = "duration"
        const val PODCAST_TIME = "podcast_time"
    }
}

@Entity(tableName = SearchResult.TABLE_NAME)
data class SearchResult(
        @PrimaryKey(autoGenerate = false)
        val query: String,
        val ids: List<String> = ArrayList(),
        val timeStamp: Long = System.currentTimeMillis()) {
    companion object {
        const val TABLE_NAME = "searchResult"
        const val QUERY = "query"
        const val IDS = "ids"
        const val TIME_STAMP = "timeStamp"
    }
}

data class Progress(var duration: Long = 0L,
                    var progress: Long = 0L) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeLong(progress)
    }

    fun readFromParcel(`in`: Parcel) {
        duration = `in`.readLong()
        progress = `in`.readLong()
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Progress> {
        override fun createFromParcel(parcel: Parcel): Progress {
            return Progress(parcel)
        }

        override fun newArray(size: Int): Array<Progress?> {
            return arrayOfNulls(size)
        }
    }
}

@Entity(tableName = "article")
data class Article(
        @PrimaryKey(autoGenerate = false)
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

@Entity(tableName = "host")
data class Host(
        @PrimaryKey(autoGenerate = false)
        @SerializedName("nickname") val nickname: String,
        @SerializedName("avatar") val avatar: String? = null,
        @SerializedName("twitter") val twitter: String? = null,
        @SerializedName("instagram") val instagram: String? = null,
        @SerializedName("lurk") val lurk: String? = null)

@Entity(tableName = "")
data class PageResult(val query: String?, val ids: List<String?>, val totalCount: Int?)