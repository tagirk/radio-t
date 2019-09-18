package su.tagir.apps.radiot.ui.podcasts.downloaded

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import javax.inject.Inject

class DownloadedPodcastsFragment: BaseMvpPagedListFragment<Entry, DownloadedPodcastsContract.View, DownloadedPodcastsContract.Presenter>(),
        DownloadedPodcastsContract.View,
        Injectable {

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.isEnabled = false
    }

    override fun createPresenter(): DownloadedPodcastsContract.Presenter {
        return DownloadedPodcastsPresenter(entryRepository, scheduler, router)
    }


    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, GlideApp.with(this), presenter)

    override fun showRemoveError(error: String) {
        context?.let { c ->
            AlertDialog.Builder(c)
                    .setTitle(R.string.error)
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

}