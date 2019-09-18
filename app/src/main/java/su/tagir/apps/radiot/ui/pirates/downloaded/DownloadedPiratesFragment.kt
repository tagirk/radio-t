package su.tagir.apps.radiot.ui.pirates.downloaded

import androidx.appcompat.app.AlertDialog
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import javax.inject.Inject

class DownloadedPiratesFragment:
        BaseMvpPagedListFragment<Entry, DownloadedPiratesContract.View, DownloadedPiratesContract.Presenter>(),
        DownloadedPiratesContract.View,
        Injectable {

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, GlideApp.with(this), presenter)

    override fun createPresenter(): DownloadedPiratesContract.Presenter {
        return DownloadedPiratesPresenter(entryRepository, scheduler)
    }

    override fun showRemoveError(error: String) {
        context?.let {c ->
            AlertDialog.Builder(c)
                    .setTitle(R.string.error)
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }
}