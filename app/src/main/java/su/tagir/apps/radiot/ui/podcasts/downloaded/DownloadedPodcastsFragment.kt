package su.tagir.apps.radiot.ui.podcasts.downloaded

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import javax.inject.Inject

class DownloadedPodcastsFragment: BaseMvpPagedListFragment<Entry, DownloadedPodcastsContract.View, DownloadedPodcastsContract.Presenter>(),
        DownloadedPodcastsContract.View,
        EntriesAdapter.Callback,
        Injectable {

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    @Inject
    lateinit var router: Router

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.isEnabled = false
    }

    override fun createPresenter(): DownloadedPodcastsContract.Presenter {
        return DownloadedPodcastsPresenter(entryRepository, scheduler, router)
    }


    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, GlideApp.with(this), this)

    override fun showRemoveError(error: String?) {
        context?.let { c ->
            AlertDialog.Builder(c)
                    .setTitle(R.string.error)
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    override fun select(entry: Entry) {
        presenter.select(entry)
    }

    override fun download(entry: Entry) {}

    override fun remove(entry: Entry) {
        presenter.remove(entry)
    }

    override fun openComments(entry: Entry) {
        presenter.openComments(entry)
    }

}