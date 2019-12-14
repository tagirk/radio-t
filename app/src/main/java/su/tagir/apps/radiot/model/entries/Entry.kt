package su.tagir.apps.radiot.model.entries

import android.database.Cursor
import android.text.TextUtils
import su.tagir.apps.radiot.model.entities.EntryQueries
import java.util.*

object EntryState {
    const val PLAYING = 1
    const val PAUSED = 2
    const val IDLE = 0
}

data class Entry(
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
        val commentsCount: Int = 0,
        val commentators: List<String>? = null) {

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
                    commentsCount = 0)

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
                    progress = c.getLong(c.getColumnIndexOrThrow("progress")),
                    state = c.getInt(c.getColumnIndexOrThrow("state")),
                    file = c.getString(c.getColumnIndexOrThrow("file")),
                    downloadProgress = c.getInt(c.getColumnIndex("downloadProgress")),
                    commentsCount = c.getInt(c.getColumnIndex("commentsCount")))

    val chatUrl
        get() = "https://chat.radio-t.com/logs/radio-t-${title?.substring(8)}.html"
}

fun Entry.insert(entryQueries: EntryQueries){
    entryQueries.insert(url, title, date, categories?.joinToString(","), image, fileName, body, showNotes, audioUrl, progress, state, file, downloadId, downloadProgress, commentsCount, commentators)
}

fun Entry.update(entryQueries: EntryQueries){
    entryQueries.update(title, audioUrl, body, date, image, showNotes, fileName, url)
}

val entryMapper: (url: String,
                  title: String?,
                  date: Date?,
                  categories: String?,
                  image: String?,
                  fileName: String?,
                  body: String?,
                  showNotes: String?,
                  audioUrl: String?,
                  progress: Long,
                  state: Int,
                  file: String?,
                  downloadId: Long,
                  downloadProgress: Int,
                  commentsCount: Int,
                  commentators: List<String>?) -> Entry
    get() = { url,
              title,
              date,
              categories,
              image,
              fileName,
              body,
              showNotes,
              audioUrl,
              progress,
              state,
              file,
              downloadId,
              downloadProgress,
              commentsCount,
              commentators -> Entry(url, title, date, categories?.split(","), image, fileName, body, showNotes, audioUrl, progress, state, file, downloadId, downloadProgress, commentsCount, commentators)
    }