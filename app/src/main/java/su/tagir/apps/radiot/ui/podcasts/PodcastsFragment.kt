package su.tagir.apps.radiot.ui.podcasts

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.REQUEST_WRITE_PERMISSION
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment

class PodcastsFragment : BaseMvpListFragment<Entry, PodcastsContract.View, PodcastsContract.Presenter>(), PodcastsContract.View,
        EntriesAdapter.Callback {

    private var entryForDownload: Entry? = null
        set(value) {
            field = value
            value?.let {
                startDownload()
            }
        }

    override fun createAdapter() = EntriesAdapter(this)

    override fun createPresenter(): PodcastsContract.Presenter {
        val appComponent: AppComponent = (activity!!.application as App).appComponent
        return PodcastsPresenter(appComponent.entryRepository, appComponent.router)
    }

    override fun onResume() {
        super.onResume()
        entryForDownload?.let {
            presenter.download(entryForDownload!!)
            entryForDownload = null
        }
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != REQUEST_WRITE_PERMISSION) {
            return
        }

        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            showNeedPermission()
        }

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

    private fun startDownload() {
        if (context == null) {
            return
        }
        when {
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                entryForDownload?.let {
                    presenter.download(entryForDownload!!)
                    entryForDownload = null
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> showPermissionRationale()

            else -> requestWritePermission()
        }
    }

    private fun showNeedPermission() {
        Toast.makeText(context, getString(R.string.write_permission_rationale), Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(context!!)
                .setMessage(R.string.write_permission_rationale)
                .setPositiveButton("OK") { _, _ -> requestWritePermission() }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun requestWritePermission() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
    }
}