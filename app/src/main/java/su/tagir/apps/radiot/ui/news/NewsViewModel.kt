package su.tagir.apps.radiot.ui.news

import android.arch.paging.LivePagedListBuilder
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.model.repository.EntryRepository.Companion.PAGE_SIZE
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import javax.inject.Inject

class NewsViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    schedulerProvider: BaseSchedulerProvider,
                    private val router: Router) : ListViewModel<Entry>(schedulerProvider) {

    val error = SingleLiveEvent<String>()


    override fun getData() = LivePagedListBuilder(entryRepository.getEntries("news", "info"), PAGE_SIZE).build()


    override fun requestUpdates() {
        addDisposable(entryRepository.refreshNews()
                .observeOn(scheduler.ui())
                .subscribe({ state.postValue(ViewModelState.COMPLETE) },
                        { t ->
                            if (state.value?.refreshing == true) {
                                error.value = t.message
                            }
                            state.postValue(ViewModelState.error(t.message))
                        }))
    }

    fun onEntryClick(entry: Entry) {
        router.navigateTo(Screens.CONTENT_SCREEN, entry.url)
    }

}