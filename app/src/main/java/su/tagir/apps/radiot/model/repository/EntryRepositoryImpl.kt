package su.tagir.apps.radiot.model.repository

import android.app.Application
import android.text.TextUtils
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import su.tagir.apps.radiot.model.api.RemarkClient
import su.tagir.apps.radiot.model.api.RestClient
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.entries.*
import su.tagir.apps.radiot.service.AudioService
import su.tagir.apps.radiot.utils.timeOfDay
import java.util.*
import kotlin.collections.HashMap

class EntryRepositoryImpl(private val restClient: RestClient,
                          private val remarkClient: RemarkClient,
                          private val database: RadiotDb,
                          private val downloadManager: DownloadManager,
                          private val application: Application) : EntryRepository {

    companion object {
        const val PAGE_SIZE = 20
    }

    private val entryQueries = database.entryQueries
    private val timeLabelQueries = database.timeLabelQueries
    private val pageResultQueries = database.pageResultQueries

    override fun getCurrent(): Flow<Entry> =
            entryQueries.findCurrentPlaying(mapper = entryMapper).asFlow().mapToOne()

    override fun getTimeLabels(entry: Entry?): Flow<List<TimeLabel>> =
            timeLabelQueries.findByPodcastTime(entry?.date, mapper = timeLabelMapper).asFlow().mapToList()


    override suspend fun refreshPodcasts() {
        val podcasts = restClient.getPosts(PAGE_SIZE, "podcast")
        val commentsCount = remarkClient.getCommentsCount(urls = podcasts.map { it.url })
        database.transaction {
            insertRTEntries(podcasts)
            commentsCount.forEach { info ->
                entryQueries.updateCommentsCount(info.count, info.url)
            }
        }

    }


    override suspend fun refreshPirates() {
//        return Single.create(SingleOnSubscribe<List<RTEntry>> { emitter ->
//            try {
//                val connection = URL("https://feeds.feedburner.com/pirate-radio-t").openConnection() as HttpURLConnection
//                connection.requestMethod = "GET"
//                connection.addRequestProperty("Accept", "application/xml")
//                connection.doInput = true
//                connection.connect()
//                val podcasts = PiratesParser.parsePirates(connection.inputStream)
//                if (!emitter.isDisposed) {
//                    emitter.onSuccess(podcasts)
//                }
//            } catch (e: Exception) {
//                Timber.e(e)
//                if (!emitter.isDisposed) {
//                    emitter.onError(e)
//                }
//            }
//        })
//                .doOnSuccess { entryDao.saveRadioTEntries(it) }
//                .ignoreElement()
    }

    override fun getEntries(vararg categories: String): Flow<List<Entry>> =
            entryQueries.findByCategories(categories.asList(), entryMapper).asFlow().mapToList()


    override fun getDownloadedEntries(vararg categories: String): Flow<List<Entry>> =
            entryQueries.findDownloadedByCategories(categories.asList(), mapper = entryMapper).asFlow().mapToList()

    override suspend fun refreshNews() {
        val news = restClient.getPosts(PAGE_SIZE, "news,info")
        database.transaction {
            insertRTEntries(news)
        }
    }

    override suspend fun search(query: String) {
        val entries = restClient.search(query, 0, PAGE_SIZE)
        database.transaction {
            pageResultQueries.insert(query, entries.map { it.url }, null, Date())
            insertRTEntries(entries)
        }
    }

    override suspend fun searchNextPage(query: String, skip: Int): Boolean {
        val entries = restClient.search(query, skip, PAGE_SIZE)
        database.transaction {
            mergeAndsavePageResult(query, entries)
        }
        return entries.isNotEmpty()
    }


    override fun getRecentSearches(): Flow<List<String>> =
            pageResultQueries.findRecentSearches().asFlow().mapToList()


    @ExperimentalCoroutinesApi
    override fun getForQuery(query: String): Flow<List<Entry>> =
            pageResultQueries.findByQuery(query, pageResultMapper)
                    .asFlow()
                    .mapToOne()
                    .flatMapLatest { result ->
                        val idsStr = "'${TextUtils.join("','", result.ids)}'"
                        entryQueries.findByIds(idsStr, entryMapper).asFlow().mapToList()
                    }

    override suspend fun removeQuery(query: String) {
        pageResultQueries.removeQuery(query)
    }

    override suspend fun startDownload(url: String?) {
        val downloadId = downloadManager.startDownload(url)
        entryQueries.updateDownloadId(downloadId, url)
    }

    override suspend fun checkDownloadStatus() {
        val ids = entryQueries.selectDownloadIds().executeAsList().toMutableList()
        if (ids.isEmpty()) {
            return
        }
        val downloadProgressMap = HashMap<Long, Int>()
        val fileNames = HashMap<Long, String>()
        downloadManager.checkDownloadStatus(ids, downloadProgressMap, fileNames)


        database.transaction {
            ids.forEach { id ->
                entryQueries.resetDownloadProgress(id)
            }

            downloadProgressMap.forEach {
                entryQueries.updateDownloadProgress(it.value, it.key)
            }

            fileNames
                    .forEach {
                        entryQueries.saveFilePath(it.value, it.key)
                    }
        }
    }

    override suspend fun deleteFile(id: Long) {
        downloadManager.delete(id)
        entryQueries.deleteFilePath(id)
    }

    override fun play(podcast: Entry) {
        AudioService.play(podcast.file, podcast.audioUrl, podcast.progress, application)
    }

    override fun pause() {
        AudioService.pause(application)
    }

    override fun resume() {
        AudioService.resume(application)
    }

    override suspend fun playStream(url: String) {
        database.transaction {
            entryQueries.resetStates(EntryState.IDLE, EntryState.IDLE)
            val streamEntry = Entry(url = url, title = "Online вещание", audioUrl = url, state = EntryState.PAUSED, categories = listOf("online_stream"))
            streamEntry.insert(entryQueries)
        }
        AudioService.play(null, url, 0L, application)
    }

    override suspend fun setCurrentEntry(audioUrl: String?, lastProgress: Long) {
        database.transaction {
            if (lastProgress > 0) {
                entryQueries.updateCurrentPlayingEntryProgress(lastProgress)
            }
            entryQueries.resetStates(EntryState.IDLE, EntryState.IDLE)
            if (audioUrl != null) {
                entryQueries.updateState(state = EntryState.PAUSED, audioUrl = audioUrl)
            }
        }
    }

    override suspend fun updateCurrentEntryStateAndProgress(state: Int, progress: Long) {
        database.transaction {
            if (state != EntryState.PLAYING && progress > 0) {
                entryQueries.updateCurrentPlayingEntryProgress(progress)
            }
            entryQueries.updateCurrentPlayingEntryState(state)
        }
    }

    override fun getEntry(id: String): Flow<Entry> = entryQueries.findByUrl(id, entryMapper).asFlow().mapToOne()

    private fun insertRTEntries(rtEntries: List<RTEntry>) {
        rtEntries.forEach { podcast ->
            val count = entryQueries.count(podcast.url).executeAsOne()
            val entry = Entry(podcast)
            if (count == 0L) {
                entry.insert(entryQueries)
            } else {
                entry.update(entryQueries)
            }
            podcast
                    .timeLabels
                    ?.forEach { timeLabel ->
                        timeLabelQueries.insert(timeLabel.topic, timeLabel.time?.timeOfDay(), timeLabel.duration, timeLabel.time)
                    }
        }
    }

    private fun mergeAndsavePageResult(query: String, entries: List<RTEntry>, totalCount: Int? = null) {
        val current = pageResultQueries.findByQuery(query, mapper = pageResultMapper).executeAsOneOrNull()
                ?: PageResult(query, emptyList(), totalCount)
        val merged = current.ids.toMutableList()
        merged.addAll(entries.map { it.url })
        pageResultQueries.insert(query, merged, totalCount, Date())
        insertRTEntries(entries)
    }
}