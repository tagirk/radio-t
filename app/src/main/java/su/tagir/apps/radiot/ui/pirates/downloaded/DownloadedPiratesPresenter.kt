package su.tagir.apps.radiot.ui.pirates.downloaded

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBaseListPresenter

class DownloadedPiratesPresenter(private val entryRepository: EntryRepository,
                                dispatcher: CoroutineDispatcher = MainDispatcher()) :
        MvpBaseListPresenter<Entry, DownloadedPiratesContract.View>(dispatcher), DownloadedPiratesContract.Presenter {

    private val deleterErrorHandler by lazy {
        CoroutineExceptionHandler { _, exception ->
            ifViewAttached({v -> v.showRemoveError(exception.message)})
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        launch {
            entryRepository
                    .getDownloadedEntries(listOf("pirates"))
                    .collect{data ->  state = state.copy(data = data)}
        }
    }

    override fun select(entry: Entry) {
        entryRepository.play(entry)
    }


    override fun remove(entry: Entry) {
        launch(deleterErrorHandler) {
            entryRepository.deleteFile(entry.downloadId)
        }
    }

}