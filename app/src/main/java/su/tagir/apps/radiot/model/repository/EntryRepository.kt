package su.tagir.apps.radiot.model.repository

import android.app.Application
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.*
import su.tagir.apps.radiot.model.api.RemarkClient
import su.tagir.apps.radiot.model.api.RestClient
import su.tagir.apps.radiot.model.db.EntryDao
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.RTEntry
import su.tagir.apps.radiot.model.entries.SearchResult
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.parser.PiratesParser
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.service.AudioService
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(private val restClient: RestClient,
                                          private val remarkClient: RemarkClient,
                                          private val entryDao: EntryDao,
                                          private val downloadManager: DownloadManager,
                                          private val application: Application,
                                          private val scheduler: BaseSchedulerProvider) {

    companion object {
        const val PAGE_SIZE = 20
    }

    fun getCurrent(): Flowable<Entry> =
            entryDao.getCurrentEntryLive().distinctUntilChanged().subscribeOn(scheduler.io())

    fun getTimeLabels(entry: Entry?): Flowable<List<TimeLabel>> =
            entryDao.getTimeLabels(entry?.date).distinctUntilChanged().subscribeOn(scheduler.io())


    fun refreshPodcasts(): Completable =
            restClient
                    .getPosts(PAGE_SIZE, "podcast")
                    .doOnSuccess { entryDao.saveRadioTEntries(it) }
                    .map { entries -> entries.map { it.url } }
                    .flatMap { remarkClient.getCommentsCount(urls = it) }
                    .doOnSuccess { entryDao.updateEntriesCommentsCount(it) }
                    .ignoreElement()

    fun refreshPirates(): Completable {
        return Single.create(SingleOnSubscribe<List<RTEntry>> { emitter ->
            try {
                val connection = URL("https://feeds.feedburner.com/pirate-radio-t").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.addRequestProperty("Accept", "application/xml")
                connection.doInput = true
                connection.connect()
                val podcasts = PiratesParser.parsePirates(connection.inputStream)
                if(!emitter.isDisposed) {
                    emitter.onSuccess(podcasts)
                }
            } catch (e: Exception) {
                if(!emitter.isDisposed) {
                    emitter.onError(e)
                }
            }
        })
                .doOnSuccess { entryDao.saveRadioTEntries(it) }
                .ignoreElement()
    }

    fun getEntries(vararg categories: String) =
            RxPagedListBuilder(entryDao.getEntries(categories), PAGE_SIZE)
                    .setFetchScheduler(scheduler.io())
                    .setNotifyScheduler(scheduler.ui())
                    .buildFlowable(BackpressureStrategy.LATEST)

    fun getDownloadedEntries(vararg categories: String): Flowable<PagedList<Entry>> =
            RxPagedListBuilder(entryDao.getDownloadedEntries(categories), PAGE_SIZE)
                    .setFetchScheduler(scheduler.io())
                    .setNotifyScheduler(scheduler.ui())
                    .buildFlowable(BackpressureStrategy.LATEST)
                    .distinctUntilChanged()

    fun refreshNews(): Completable =
            restClient
                    .getPosts(PAGE_SIZE, "news,info")
                    .observeOn(scheduler.io())
                    .doOnSuccess { entryDao.saveRadioTEntries(it) }
                    .ignoreElement()


    fun search(query: String): Completable =
            restClient
                    .search(query, 0, PAGE_SIZE)
                    .observeOn(scheduler.io())
                    .doOnSuccess {entries -> entryDao.saveSearchResult(SearchResult(query, entries.map { it.url }), entries) }
                    .ignoreElement()

    fun searchNextPage(query: String, skip: Int): Single<Boolean> =
            restClient
                    .search(query, skip, PAGE_SIZE)
                    .observeOn(scheduler.io())
                    .doOnSuccess {
                        if (it.isNotEmpty()) {
                            entryDao.mergeAndsaveSearchResult(query, it)
                        }
                    }
                    .map { it.isNotEmpty() }


    fun getRecentSearches() =
            RxPagedListBuilder(entryDao.findRecentSearches(), PAGE_SIZE)
                    .setFetchScheduler(scheduler.io())
                    .setNotifyScheduler(scheduler.ui())
                    .buildFlowable(BackpressureStrategy.LATEST)

    fun getForQuery(query: String): Flowable<List<Entry>> {
        return entryDao
                .findSearchResultLive(query)
                .flatMapSingle { entryDao.loadById(it.ids)}
                .subscribeOn(scheduler.io())
    }

    fun removeQuery(query: String?) {
        scheduler.computation().createWorker().schedule {
            entryDao.removeQuery(query)
        }
    }

    fun startDownload(url: String?): Completable {
        return Completable.fromCallable {
            val downloadId = downloadManager.startDownload(url)
            entryDao.updateDownloadId(downloadId, url)
            return@fromCallable url
        }
    }

    fun checkDownloadStatus() {
        val ids = entryDao.getDownloadIds().toMutableList()
        if (ids.isEmpty()) {
            return
        }
        val downloadProgressMap = HashMap<Long, Int>()
        val fileNames = HashMap<Long, String>()
        downloadManager.checkDownloadStatus(ids, downloadProgressMap, fileNames)
        entryDao.updateDownloadStatus(ids, downloadProgressMap, fileNames)
    }

    fun deleteFile(id: Long): Completable {
        return Completable.fromCallable {
            downloadManager.delete(id)
            entryDao.deleteFilePath(id)
            return@fromCallable id
        }
    }

    fun play(podcast: Entry) {
        AudioService.play(podcast.file, podcast.audioUrl, podcast.progress, application)
    }

    fun pause() {
        AudioService.pause(application)
    }

    fun resume() {
        AudioService.resume(application)
    }

    fun playStream(url: String) {
        entryDao.playStream(url)
        AudioService.play(null, url, 0L, application)
    }

    fun setCurrentEntry(audioUrl: String?, lastProgress: Long) {
        entryDao.setCurrentEntry(audioUrl, lastProgress)
    }

    fun updateCurrentEntryStateAndProgress(state: Int, progress: Long) {
        entryDao.updateCurrentEntryStateAndProgress(state, progress)
    }

    fun getEntry(id: String?): Maybe<Entry?> = entryDao.getEntry(id)
}