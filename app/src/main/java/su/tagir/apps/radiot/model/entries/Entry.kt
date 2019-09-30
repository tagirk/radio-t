package su.tagir.apps.radiot.model.entries

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
        val commentsCount: Int = 0) {

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

    val chatUrl
        get() = "https://chat.radio-t.com/logs/radio-t-${title?.substring(8)}.html"
}

fun Entry.insert(entryQueries: EntryQueries){
    entryQueries.insert(url, title, date, categories, image, fileName, body, showNotes, audioUrl, progress, state, file, downloadId, downloadProgress, commentsCount)
}

fun Entry.update(entryQueries: EntryQueries){
    entryQueries.update(title, audioUrl, body, date, image, showNotes, fileName, url)
}

val entryMapper: (url: String,
                  title: String?,
                  date: Date?,
                  categories: List<String>?,
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
                  commentsCount: Int) -> Entry
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
              commentsCount -> Entry(url, title, date, categories, image, fileName, body, showNotes, audioUrl, progress, state, file, downloadId, downloadProgress, commentsCount)
    }