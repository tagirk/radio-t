package su.tagir.apps.radiot.model.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import su.tagir.apps.radiot.model.api.RestClient
import su.tagir.apps.radiot.model.db.EntryDao
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.RTEntry
import su.tagir.apps.radiot.model.entries.SearchResult
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.service.AudioService
import su.tagir.apps.radiot.ui.common.AbsentLiveData
import su.tagir.apps.radiot.utils.getDistinct
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(private val restClient: RestClient,
                                          private val entryDao: EntryDao,
                                          private val downloadManager: DownloadManager,
                                          private val application: Application) {

    companion object {
        const val PAGE_SIZE = 20
    }

    fun getCurrent(): LiveData<Entry> =
            entryDao.getCurrentEntryLive().getDistinct()

    fun getTimeLabels(entry: Entry?): LiveData<List<TimeLabel>> =
            entryDao.getTimeLabels(entry?.date).getDistinct()


    fun refreshPodcasts(): Completable =
            restClient
                    .getPosts(PAGE_SIZE, "podcast")
                    .doOnSuccess { entryDao.saveRadioTEntries(it) }
                    .toCompletable()

    fun getPodcasts() = entryDao.getPodcasts()

    fun refreshNews(): Completable =
            restClient
                    .getPosts(PAGE_SIZE, "news,info")
                    .doOnSuccess { entryDao.saveRadioTEntries(it) }
                    .toCompletable()

    fun getNews() =
            entryDao.getNews()


    fun search(query: String): Completable =
            restClient
                    .search(query, 0, PAGE_SIZE)
                    .doOnSuccess {
                        entryDao.saveSearchResult(SearchResult(query, it.map { it.url }), it)
                    }
                    .toCompletable()

    fun searchNextPage(query: String): Completable {
        return Single.just(entryDao.findSearchResult(query) ?: SearchResult(query, emptyList(), 0L))
                .flatMap {
                    Single.zip(Single.just(it), restClient.search(query, it.ids.size, PAGE_SIZE),
                            BiFunction<SearchResult, List<RTEntry>, List<RTEntry>> { current, entries ->
                                val merged = current.ids.toMutableList()
                                merged.addAll(entries.map { it.url })
                                entryDao.saveSearchResult(SearchResult(query, merged), entries)
                                entries
                            })
                }
                .toCompletable()
    }

    fun getRecentSearches() = entryDao.findRecentSearches()

    fun getForQuery(query: String): LiveData<List<Entry>> {
        return Transformations
                .switchMap(entryDao.findSearchResultLive(query), { searchResult ->
                    if (searchResult == null) {
                        AbsentLiveData()
                    } else {
                        entryDao.loadById(searchResult.ids)
                    }
                })
    }

    fun removeQuery(query: String?) {
        entryDao.removeQuery(query)
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
        Timber.d("ids: ${ids.size}, progress: ${downloadProgressMap.size}, fileNames: ${fileNames.size}")
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

    fun getEntry(id: String?): LiveData<Entry?> = entryDao.getEntry(id)
}