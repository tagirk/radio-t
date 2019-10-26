package su.tagir.apps.radiot.model.repository

import kotlinx.coroutines.flow.Flow
import su.tagir.apps.radiot.model.entries.Entry

import su.tagir.apps.radiot.model.entries.TimeLabel

interface EntryRepository {

    fun getCurrent(): Flow<Entry?>

    fun getTimeLabels(entry: Entry?): Flow<List<TimeLabel>>

    suspend fun refreshEntries(categories: Array<String>, force: Boolean = false)

    suspend fun loadCommentators()

    suspend fun refreshPirates(force: Boolean = false)

    fun getEntries(categories: Array<String>): Flow<List<Entry>>

    fun getDownloadedEntries(vararg categories: String): Flow<List<Entry>>

    suspend fun search(query: String)

    suspend fun searchNextPage(query: String, skip: Int): Boolean

    fun getRecentSearches(): Flow<List<String>>

    fun getForQuery(query: String): Flow<List<Entry>>

    suspend fun removeQuery(query: String)

    suspend fun startDownload(url: String?)

    suspend fun checkDownloadStatus()

    suspend fun deleteFile(id: Long)

    fun play(podcast: Entry)

    fun pause()

    fun resume()

    suspend fun playStream(url: String)

    fun setCurrentEntry(audioUrl: String?, lastProgress: Long)

    fun updateCurrentEntryStateAndProgress(state: Int, progress: Long)

    fun getEntry(id: String): Flow<Entry>
}