package su.tagir.apps.radiot.ui.pirates.downloaded

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter

class DownloadedPiratesPresenter(private val entryRepository: EntryRepository,
                                dispatcher: CoroutineDispatcher = MainDispatcher()) :
        BaseListPresenter<Entry, DownloadedPiratesContract.View>(dispatcher), DownloadedPiratesContract.Presenter {

    private val deleterErrorHandler by lazy {
        CoroutineExceptionHandler { _, exception ->
            view?.showRemoveError(exception.message)
        }
    }

    override fun doOnAttach(view: DownloadedPiratesContract.View) {
        observePodcasts()
    }

    override fun loadData(pullToRefresh: Boolean) {

    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }


    override fun remove(entry: Entry) {
        launch(deleterErrorHandler) {
            entryRepository.deleteFile(entry.downloadId)
        }
    }

    private fun observePodcasts() {
       launch {
           entryRepository
                   .getDownloadedEntries("pirates")
                   .collect{data ->  state = state.copy(data = data)}
       }
    }
}