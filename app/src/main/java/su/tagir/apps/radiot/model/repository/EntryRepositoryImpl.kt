package su.tagir.apps.radiot.model.repository

import android.app.Application
import com.squareup.sqldelight.runtime.coroutines.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.invoke
import su.tagir.apps.radiot.model.api.RemarkClient
import su.tagir.apps.radiot.model.api.RestClient
import su.tagir.apps.radiot.model.db.RadiotDb
import su.tagir.apps.radiot.model.entries.*
import su.tagir.apps.radiot.model.parser.PiratesParser
import su.tagir.apps.radiot.service.AudioService
import su.tagir.apps.radiot.utils.timeOfDay
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class EntryRepositoryImpl(private val restClient: RestClient,
                          private val remarkClient: RemarkClient,
                          private val database: RadiotDb,
                          private val downloadManager: DownloadManager,
                          private val application: Application,
                          private val dispatcher: CoroutineDispatcher = Dispatchers.Default) : EntryRepository {

    companion object {
        const val PAGE_SIZE = 50
    }

    private val entryQueries = database.entryQueries
    private val timeLabelQueries = database.timeLabelQueries
    private val pageResultQueries = database.pageResultQueries
    private val searchResultQueries = database.searchResultQueries

    private val refreshLimiter = RateLimiter<String>(5, TimeUnit.MINUTES)

    override fun getCurrent(): Flow<Entry?> =
            entryQueries.findCurrentPlaying(mapper = entryMapper).asFlow().mapToOneOrNull(dispatcher)

    override fun getTimeLabels(entry: Entry?): Flow<List<TimeLabel>> =
            timeLabelQueries.findByPodcastTime(entry?.date, mapper = timeLabelMapper).asFlow().mapToList(dispatcher)


    @ExperimentalCoroutinesApi
    override suspend fun refreshEntries(categories: List<String>, force: Boolean) {
        if(!refreshLimiter.shouldFetch(categories.toString()) && !force){
            return
        }
        val podcasts = restClient.getPosts(PAGE_SIZE, categories.joinToString(separator = ","))
        val commentsCount = remarkClient.getCommentsCount(urls = podcasts.map { it.url })
        dispatcher {
            database.transaction {
                insertRTEntries(podcasts)
                commentsCount.forEach { info ->
                    entryQueries.updateCommentsCount(info.count, info.url)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun loadCommentators() {
        if(!refreshLimiter.shouldFetch("commentators")){
            return
        }
        dispatcher{
            val preps = database.entryQueries
                    .findByCategories(listOf("prep"), entryMapper)
                    .executeAsList()
                    .filter { e ->
                        (e.commentators?.size ?: 0) < 10 ||
                        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - (e.date?.time ?: 0)) < 5
                    }
            for (e in preps) {
                val commentators = remarkClient.getCommentsList(postUrl = e.url)
                        .comments
                        .mapNotNull { c -> c.user?.picture }
                        .asSequence()
                        .filter { s -> s.isNotBlank() }
                        .distinct()
                        .toList()

                database.entryQueries.updateCommentators(commentators = commentators, url = e.url)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun refreshPirates(force: Boolean) {
        if(!refreshLimiter.shouldFetch("pirates") && !force){
            return
        }
        dispatcher.invoke {
            val connection = URL("https://feeds.feedburner.com/pirate-radio-t").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.addRequestProperty("Accept", "application/xml")
            connection.doInput = true
            connection.connect()
            val podcasts = PiratesParser.parsePirates(connection.inputStream)
            insertRTEntries(podcasts)
        }
    }

    override fun getEntries(categories: List<String>): Flow<List<Entry>> {
        return entryQueries.findByCategories(categories, entryMapper).asFlow().mapToList(dispatcher)
    }


    override fun getDownloadedEntries(categories: List<String>): Flow<List<Entry>> {
        return entryQueries.findDownloadedByCategories(categories, mapper = entryMapper).asFlow().mapToList(dispatcher)
    }


    override suspend fun search(query: String) {
        val entries = restClient.search(query, 0, PAGE_SIZE)
        database.transaction {
            searchResultQueries.insert(query, entries.map { it.url }, Date())
            insertRTEntries(entries)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun searchNextPage(query: String, skip: Int): Boolean {
        val entries = restClient.search(query, skip, PAGE_SIZE)
        return dispatcher {
            database.transaction {
                mergeAndSaveSearchResult(query, entries)
            }
            entries.isNotEmpty()
        }
    }


    override fun getRecentSearches(): Flow<List<String>> =
            searchResultQueries.findRecentSearches().asFlow().mapToList(dispatcher)


    @ExperimentalCoroutinesApi
    override fun getForQuery(query: String): Flow<List<Entry>> =
            searchResultQueries.findByQuery(query, searchResultMapper)
                    .asFlow()
                    .mapToOneOrDefault(SearchResult(query, emptyList(), Date()))
                    .flatMapLatest { result ->
                        entryQueries.findByIds(result.ids, entryMapper).asFlow().mapToList(dispatcher)
                    }

    @ExperimentalCoroutinesApi
    override suspend fun removeQuery(query: String) {
        dispatcher {
            pageResultQueries.removeQuery(query)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun startDownload(url: String?) {
        val downloadId = downloadManager.startDownload(url)
        dispatcher {
            Timber.d("startDownload: $downloadId")
            entryQueries.updateDownloadId(downloadId, url)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun checkDownloadStatus() {
        val ids = entryQueries.selectDownloadIds().executeAsList().toMutableList()
        if (ids.isEmpty()) {
            return
        }
        val downloadProgressMap = HashMap<Long, Int>()
        val fileNames = HashMap<Long, String>()
        downloadManager.checkDownloadStatus(ids, downloadProgressMap, fileNames)

        dispatcher {
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
    }

    @ExperimentalCoroutinesApi
    override suspend fun deleteFile(id: Long) {
        downloadManager.delete(id)
        dispatcher {
            entryQueries.deleteFilePath(id)
        }
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

    @ExperimentalCoroutinesApi
    override suspend fun playStream(url: String) {
        dispatcher {
            database.transaction {
                entryQueries.resetStates(EntryState.IDLE, EntryState.IDLE)
                val streamEntry = Entry(url = url, title = "Online вещание", audioUrl = url, state = EntryState.PAUSED, categories = listOf("online_stream"))
                streamEntry.insert(entryQueries)
            }
        }
        AudioService.play(null, url, 0L, application)
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
                        timeLabelQueries.insert(timeLabel.topic, timeLabel.time?.timeOfDay(), timeLabel.duration, podcast_time = podcast.date)
                    }
        }
    }

    private fun mergeAndSaveSearchResult(query: String, entries: List<RTEntry>) {
        val current = searchResultQueries.findByQuery(query, mapper = searchResultMapper).executeAsOneOrNull()
                ?: SearchResult(query, emptyList())
        val merged = current.ids.toMutableList()
        merged.addAll(entries.map { it.url })
        searchResultQueries.insert(query, merged, Date())
        insertRTEntries(entries)
    }
}