package su.tagir.apps.radiot.ui.hosts

import android.arch.paging.LivePagedListBuilder
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Host
import su.tagir.apps.radiot.model.repository.EntryRepository.Companion.PAGE_SIZE
import su.tagir.apps.radiot.model.repository.HostRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import timber.log.Timber
import javax.inject.Inject

class HostsViewModel
@Inject constructor(private val hostRepository: HostRepository,
                    private val router: Router,
                    schedulerProvider: BaseSchedulerProvider) : ListViewModel<Host>(schedulerProvider) {


    val error = SingleLiveEvent<String?>()

    override fun getData() = LivePagedListBuilder(hostRepository.getHosts(), PAGE_SIZE).build()

    override fun requestUpdates() {
        addDisposable(hostRepository
                .refreshHosts()
                .observeOn(scheduler.ui())
                .subscribe({ state.postValue(ViewModelState.COMPLETE) },
                        {
                            Timber.e(it)
                            if (state.value?.refreshing == true) {
                                error.value = it.message
                            }
                            state.postValue(ViewModelState.error(it.message))
                        }))
    }

    fun openSocialNet(url: String) {
        router.navigateTo(Screens.RESOLVE_ACTIVITY, url)
    }
}