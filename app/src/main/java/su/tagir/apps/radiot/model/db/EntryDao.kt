package su.tagir.apps.radiot.model.db

import android.database.Cursor
import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import su.tagir.apps.radiot.model.entries.*
import su.tagir.apps.radiot.utils.timeOfDay
import timber.log.Timber
import java.util.*

@Dao
abstract class EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEntry(entry: Entry?): Long

    @Query("UPDATE ${Entry.TABLE_NAME} SET " +
            "${Entry.TITLE} = :title, " +
            "${Entry.AUDIO_URl} = :audioUrl, " +
            "${Entry.BODY} = :body, " +
            "${Entry.DATE} = :date, " +
            "${Entry.IMAGE} = :image, " +
            "${Entry.SHOWNOTES} = :showNotes, " +
            "${Entry.FILE_NAME} = :fileName " +
            "WHERE ${Entry.URL} = :url")
    abstract fun updateEntry(url: String, title: String, audioUrl: String?, body: String?,
                             date: Date, image: String?, showNotes: String?, fileName: String?)

    @Query("UPDATE entry SET commentsCount = :count WHERE url = :url")
    abstract fun updateCommentsCount(url: String, count:Int)

    @Transaction
    open fun updateEntriesCommentsCount(infos:List<PostInfo>){
        infos.forEach {
            updateCommentsCount(it.url, it.count)
        }
    }

    @Query("SELECT url FROM entry WHERE url = :url")
    abstract fun findUrl(url: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTimeLabel(label: TimeLabel): Long

    @Transaction
    open fun saveRadioTEntries(entries: List<RTEntry>?) {
        saveEntries(entries)
    }

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.URL} = :url LIMIT 1")
    abstract fun getEntry(url: String?): Maybe<Entry>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.CATEGORIES} IN (:categories) ORDER BY ${Entry.DATE} DESC")
    abstract fun getEntries(categories: Array<out String>): DataSource.Factory<Int, Entry>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.CATEGORIES} IN (:categories) AND ${Entry.FILE} != '' ORDER BY ${Entry.DATE} DESC")
    abstract fun getDownloadedEntries(categories: Array<out String>): DataSource.Factory<Int, Entry>

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.STATE} = :state WHERE ${Entry.STATE} != :state")
    abstract fun resetStates(state: Int): Int

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.STATE} = :state WHERE ${Entry.AUDIO_URl} == :audioUrl")
    abstract fun updateState(state: Int, audioUrl: String): Int

    @Transaction
    open fun setCurrentEntry(audioUrl: String?, lastProgress: Long) {
        if (lastProgress > 0) {
            updateCurrentEntryProgress(lastProgress)
        }
        resetStates(EntryState.IDLE)
        if (audioUrl != null) {
            updateState(state = EntryState.PAUSED, audioUrl = audioUrl)
        }
    }

    @Transaction
    open fun playStream(audioUrl: String) {
        resetStates(EntryState.IDLE)
        val streamEntry = Entry(url = audioUrl, title = "Online вещание", audioUrl = audioUrl, state = EntryState.PAUSED, categories = listOf("online_stream"))
        insertEntry(streamEntry)
    }

    @Query("SELECT * FROM ${Entry.TABLE_NAME} " +
            "WHERE state = ${EntryState.PAUSED} OR state = ${EntryState.PLAYING} LIMIT 1")
    abstract fun getCurrentEntryLive(): Flowable<Entry>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.STATE} = ${EntryState.PAUSED} OR ${Entry.STATE} = ${EntryState.PLAYING} LIMIT 1")
    abstract fun getCurrentEntryCursor(): Cursor

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.STATE} = :state WHERE ${Entry.STATE} != ${EntryState.IDLE}")
    abstract fun updateCurrentEntryState(state: Int)

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.PROGRESS} = :progress WHERE ${Entry.STATE} = ${EntryState.PAUSED} OR ${Entry.STATE} = ${EntryState.PLAYING}")
    abstract fun updateCurrentEntryProgress(progress: Long)

    @Transaction
    open fun updateCurrentEntryStateAndProgress(state: Int, progress: Long) {
        if (state != EntryState.PLAYING && progress > 0) {
            updateCurrentEntryProgress(progress)
        }
        updateCurrentEntryState(state)
    }

    @Query("UPDATE ${Entry.TABLE_NAME} SET  ${Entry.DOWNLOAD_ID} = :downloadId," +
            "${Entry.DOWNLOAD_PROGRESS} = 0 WHERE  ${Entry.AUDIO_URl} == :audioUrl")
    abstract fun updateDownloadId(downloadId: Long, audioUrl: String?)

    @Query("UPDATE ${Entry.TABLE_NAME} " +
            "SET ${Entry.FILE} = :file, ${Entry.DOWNLOAD_PROGRESS} = -1 " +
            "WHERE ${Entry.DOWNLOAD_ID} == :downloadId")
    abstract fun saveFilePath(file: String?, downloadId: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} " +
            "SET ${Entry.FILE} = null, ${Entry.DOWNLOAD_PROGRESS} = -1, ${Entry.DOWNLOAD_ID} = -1 " +
            "WHERE ${Entry.DOWNLOAD_ID} == :id")
    abstract fun deleteFilePath(id: Long?)

    @Query("SELECT ${Entry.DOWNLOAD_ID} FROM ${Entry.TABLE_NAME} WHERE ${Entry.DOWNLOAD_ID} != -1 AND ${Entry.DOWNLOAD_PROGRESS} != -1")
    abstract fun getDownloadIds(): List<Long>

    @Query("UPDATE ${Entry.TABLE_NAME} SET ${Entry.DOWNLOAD_PROGRESS} = :progress " +
            "WHERE ${Entry.DOWNLOAD_ID} = :downloadId")
    abstract fun updateDownloadProgress(progress: Int, downloadId: Long)

    @Query("UPDATE ${Entry.TABLE_NAME} " +
            "SET ${Entry.DOWNLOAD_PROGRESS} = -1, ${Entry.DOWNLOAD_ID} = -1 " +
            "WHERE ${Entry.DOWNLOAD_ID} = :downloadId")
    abstract fun resetDownloadProgress(downloadId: Long)

    @Transaction
    open fun updateDownloadStatus(failedIds: List<Long>?, progressMap: Map<Long, Int>?, filePaths: Map<Long, String?>?) {
        failedIds
                ?.forEach { id ->
                    resetDownloadProgress(id)
                    Timber.d("resetDownloadProgress $id")
                }

        progressMap
                ?.forEach {
                    updateDownloadProgress(it.value, it.key)
                    Timber.d("updateDownloadProgress $it")
                }

        filePaths
                ?.forEach {
                    saveFilePath(it.value, it.key)
                    Timber.d("saveFilePath $it")
                }
    }

    @Query("SELECT * FROM ${TimeLabel.TABLE_NAME} " +
            "WHERE ${TimeLabel.PODCAST_TIME} = :podcastTime ORDER BY ${TimeLabel.TIME} DESC")
    abstract fun getTimeLabels(podcastTime: Date?): Flowable<List<TimeLabel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSearchResult(searchResult: SearchResult?)

    @Query("SELECT * FROM ${SearchResult.TABLE_NAME} WHERE ${SearchResult.QUERY} = :query")
    abstract fun findSearchResult(query: String?): SearchResult?

    @Query("SELECT * FROM ${SearchResult.TABLE_NAME} WHERE ${SearchResult.QUERY} = :query")
    abstract fun findSearchResultLive(query: String): Flowable<SearchResult>

    @Query("SELECT * FROM ${Entry.TABLE_NAME} WHERE ${Entry.URL} IN (:ids) ORDER BY ${Entry.DATE} DESC")
    abstract fun loadById(ids: List<String>): Single<List<Entry>>

    @Transaction
    open fun saveSearchResult(searchResult: SearchResult?, entries: List<RTEntry>) {
        insertSearchResult(searchResult)
        saveEntries(entries)
    }

    @Transaction
    open fun mergeAndsaveSearchResult(query: String,  entries: List<RTEntry>) {
        val current = findSearchResult(query)?: SearchResult(query, emptyList())
        val merged = current.ids.toMutableList()
        merged.addAll(entries.map { it.url })
        insertSearchResult(SearchResult(query, merged))
        saveEntries(entries)
    }

    @Query("SELECT ${SearchResult.QUERY} FROM ${SearchResult.TABLE_NAME} ORDER BY ${SearchResult.TIME_STAMP} DESC")
    abstract fun findRecentSearches(): DataSource.Factory<Int, String>

    @Query("DELETE FROM ${SearchResult.TABLE_NAME} WHERE ${SearchResult.QUERY} = :query")
    abstract fun removeQuery(query: String?)

    private fun saveEntries(entries: List<RTEntry>?) {
        entries?.forEach { entry ->
            if (findUrl(entry.url) == null) {
                insertEntry(Entry(entry))
            } else {
                updateEntry(entry.url, entry.title, entry.audioUrl, entry.body, entry.date,
                        entry.image, entry.showNotes, entry.fileName)
            }
            entry
                    .timeLabels
                    ?.forEach { timeLabel ->
                        insertTimeLabel(TimeLabel(topic = timeLabel.topic,
                                time = timeLabel.time?.timeOfDay(),
                                duration = timeLabel.duration,
                                podcastTime = entry.date))
                    }
        }
    }

}