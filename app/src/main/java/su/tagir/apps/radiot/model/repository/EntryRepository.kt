package su.tagir.apps.radiot.model.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.TimeLabel

interface EntryRepository {

    fun getCurrent(): Flowable<Entry>

    fun getTimeLabels(entry: Entry?): Flowable<List<TimeLabel>>

    fun refreshPodcasts(): Completable

    fun refreshPirates(): Completable

    fun getEntries(vararg categories: String): Flowable<out List<Entry>>

    fun getDownloadedEntries(vararg categories: String): Flowable<out List<Entry>>

    fun refreshNews(): Completable

    fun search(query: String): Completable

    fun searchNextPage(query: String, skip: Int): Single<Boolean>

    fun getRecentSearches(): Flowable<out List<String>>

    fun getForQuery(query: String): Flowable<List<Entry>>

    fun removeQuery(query: String?)

    fun startDownload(url: String?): Completable

    fun checkDownloadStatus()

    fun deleteFile(id: Long): Completable

    fun play(podcast: Entry)

    fun pause()

    fun resume()

    fun playStream(url: String)

    fun setCurrentEntry(audioUrl: String?, lastProgress: Long)

    fun updateCurrentEntryStateAndProgress(state: Int, progress: Long)

    fun getEntry(id: String): Maybe<Entry>
}