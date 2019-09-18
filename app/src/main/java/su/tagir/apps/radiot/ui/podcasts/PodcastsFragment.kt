package su.tagir.apps.radiot.ui.podcasts

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import su.tagir.apps.radiot.ui.mvp.ViewState
import javax.inject.Inject

@RuntimePermissions
class PodcastsFragment : BaseMvpPagedListFragment<Entry, PodcastsContract.View, PodcastsContract.Presenter>(), PodcastsContract.View, Injectable {


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

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, GlideApp.with(this))

    override fun createPresenter(): PodcastsContract.Presenter {
        return PodcastsPresenter(entryRepository, scheduler, router)
    }

    override fun updateState(viewState: ViewState<List<Entry>>) {
        showHideViews(viewState)
        viewState.data?.let { data ->
            adapter.submitList(data as PagedList<Entry>)
        }
    }

    override fun showDownloadError(error: String) {
        context?.let { c ->
            AlertDialog.Builder(c)
                    .setTitle(R.string.error)
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    override fun download() {
        startDownloadWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownload() {
        presenter.download()

    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeedPermission() {
        Toast.makeText(context, "Для загрузки подкаста необходимо дать разрешение на запись.", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }
}