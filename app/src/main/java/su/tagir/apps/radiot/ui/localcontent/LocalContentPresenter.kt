package su.tagir.apps.radiot.ui.localcontent

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BasePresenter

class LocalContentPresenter(
        private val entryId: String,
        private val entryRepository: EntryRepository,
        private val router: Router,
        dispatcher: CoroutineDispatcher = MainDispatcher()) : BasePresenter<LocalContentContract.View>(dispatcher), LocalContentContract.Presenter {

    private var entry: Entry? = null

    override fun doOnAttach(view: LocalContentContract.View) {
        super.doOnAttach(view)
        loadContent(entryId)
    }

    override fun loadContent(id: String) {
        launch {
           entryRepository.getEntry(id)
                   .collect{entry ->
                       this@LocalContentPresenter.entry = entry
                       view?.showContent(entry)
                   }
        }
    }

    override fun openInBrowser() {
        entry?.url?.let {url ->
            router.navigateTo(Screens.WebScreen(url))
        }
    }

    override fun exit() {
        router.exit()
    }
}