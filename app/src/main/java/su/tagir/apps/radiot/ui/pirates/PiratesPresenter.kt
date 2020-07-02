package su.tagir.apps.radiot.ui.pirates

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status

class PiratesPresenter(private val entryRepository: EntryRepository,
                       dispatcher: CoroutineDispatcher = MainDispatcher()) : MvpBaseListPresenter<Entry, PiratesContract.View>(dispatcher), PiratesContract.Presenter {

    private var loadJob: Job? = null

    private val downloadErrorHandler by lazy {
        CoroutineExceptionHandler { _, exception ->
            ifViewAttached({v -> v.showDownloadError(exception.message)})
        }
    }

    override fun doOnAttach(view: PiratesContract.View) {
        super.doOnAttach(view)
        observePodcasts()
        startStatusTimer()
    }

    private fun observePodcasts() {
        launch {
            entryRepository
                    .getEntries(listOf("pirates"))
                    .collect {
                        state = state.copy(data = it)
                    }
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        state = state.copy(status = if (pullToRefresh) Status.REFRESHING else Status.LOADING)
        loadJob = launch {
            entryRepository.refreshPirates()
            state = state.copy(status = Status.SUCCESS)
        }
    }

    private fun startStatusTimer() {
        launch {
            while (true) {
                entryRepository.checkDownloadStatus()
                delay(3000L)
            }
        }
    }

    override fun download(entry: Entry) {
        launch(downloadErrorHandler) {
            entryRepository
                    .startDownload(entry.audioUrl)
        }
    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }

    override fun remove(entry: Entry) {
        launch(downloadErrorHandler) {
            entryRepository
                    .deleteFile(entry.downloadId)
        }
    }
}