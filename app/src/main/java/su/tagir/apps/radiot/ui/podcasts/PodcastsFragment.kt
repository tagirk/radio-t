package su.tagir.apps.radiot.ui.podcasts

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment

@RuntimePermissions
class PodcastsFragment : BaseMvpListFragment<Entry, PodcastsContract.View, PodcastsContract.Presenter>(), PodcastsContract.View,
        EntriesAdapter.Callback {


    private var entryForDownload: Entry? = null
        set(value) {
            field = value
            value?.let {
                startDownloadWithPermissionCheck()
            }
        }

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, this)

    override fun createPresenter(): PodcastsContract.Presenter {
        val appComponent: AppComponent = (activity!!.application as App).appComponent
        return PodcastsPresenter(appComponent.entryRepository, appComponent.router)
    }

    override fun showDownloadError(error: String?) {
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
        entryForDownload?.let {entry ->
            presenter.download(entry)
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeedPermission() {
        Toast.makeText(context, "Для загрузки подкаста необходимо дать разрешение на запись.", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun select(entry: Entry) {
        presenter.select(entry)
    }

    override fun download(entry: Entry) {
        entryForDownload = entry
    }

    override fun remove(entry: Entry) {
        presenter.remove(entry)
    }

    override fun openComments(entry: Entry) {
        presenter.openComments(entry)
    }
}