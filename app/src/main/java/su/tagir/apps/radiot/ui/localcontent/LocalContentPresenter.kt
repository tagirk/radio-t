package su.tagir.apps.radiot.ui.localcontent

import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import timber.log.Timber

class LocalContentPresenter(
        private val entryId: String,
        private val entryRepository: EntryRepository,
        private val router: Router,
        private val scheduler: BaseSchedulerProvider) : BasePresenter<LocalContentContract.View>(), LocalContentContract.Presenter {

    private var entry: Entry? = null

    override fun doOnAttach(view: LocalContentContract.View) {
        super.doOnAttach(view)
        loadContent(entryId)
    }

    override fun loadContent(id: String) {
        disposables += entryRepository.getEntry(id)
                .observeOn(scheduler.ui())
                .subscribe({entry ->
                    this.entry = entry
                    view?.showContent(entry)
                }, { Timber.e(it) })
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