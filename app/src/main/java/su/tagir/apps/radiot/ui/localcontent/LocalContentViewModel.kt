package su.tagir.apps.radiot.ui.localcontent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.viewmodel.BaseViewModel
import timber.log.Timber
import javax.inject.Inject

class LocalContentViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    private val router: Router,
                    scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    private val entry = MutableLiveData<Entry>()


    fun setId(id: String?) {
        disposable += entryRepository.getEntry(id)
                .subscribe({
                    entry.postValue(it)
                }, { Timber.e(it) })
    }

    fun getEntry(): LiveData<Entry> = entry

    fun openInBrowser() {
        router.navigateTo(Screens.WebScreen(entry.value!!.url))
    }
}