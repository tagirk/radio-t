package su.tagir.apps.radiot.ui.localcontent

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.common.AbsentLiveData
import javax.inject.Inject

class LocalContentViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    private val router: Router) : ViewModel() {

    private val entryId = MutableLiveData<String?>()
    private val entry: LiveData<Entry?>


    init {
        entry = Transformations.switchMap(entryId, { id ->
            if (id == null) {
                AbsentLiveData()
            } else {
                entryRepository.getEntry(id)
            }
        })
    }

    fun setId(id: String?) {
        entryId.value = id
    }

    fun getEntry() = entry

    fun openInBrowser() {
        router.navigateTo(Screens.WEB_SCREEN, entry.value?.url)
    }

    fun onBackPressed() {
        router.exit()
    }
}