package su.tagir.apps.radiot.ui.pirates

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import io.reactivex.Observable
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import javax.inject.Inject

@RuntimePermissions
class PiratesFragment:
        BaseMvpPagedListFragment<Entry, PiratesContract.View, PiratesContract.Presenter>(),
        PiratesContract.View,
        Injectable {


    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    override fun createPresenter(): PiratesContract.Presenter {
        return PiratesPresenter(entryRepository, scheduler)
    }

    override fun loadData(pullToRefresh: Boolean) {
        presenter.update()
    }

    override fun showDownloadError(error: String) {
        context?.let {c ->
            AlertDialog.Builder(c)
                    .setTitle(R.string.error)
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PIRATES, GlideApp.with(this))

    override fun entryClickRequests(): Observable<Entry> = (adapter as EntriesAdapter).entryClicks()

    override fun downloadClickRequests(): Observable<Entry> = (adapter as EntriesAdapter).downloadClicks()

    override fun removeClickRequests(): Observable<Entry> = (adapter as EntriesAdapter).removeClicks()

    override fun download() {
        startDownloadWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownload(){
        presenter.download()
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeedPermission(){
        Toast.makeText(context, "Для загрузки подкаста необходимо дать разрешение на запись.", Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }
}