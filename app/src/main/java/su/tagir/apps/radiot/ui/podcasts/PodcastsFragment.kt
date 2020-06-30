package su.tagir.apps.radiot.ui.podcasts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListFragment
import timber.log.Timber

class PodcastsFragment : MvpListFragment<Entry, PodcastsContract.View, PodcastsContract.Presenter>(), PodcastsContract.View,
        EntriesAdapter.Callback {

    private var entryForDownload: Entry? = null
        set(value) {
            field = value
            value?.let {
                startDownload()
            }
        }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        Timber.d("isGranted: $isGranted")
        if(isGranted){
            startDownload()
        }else{
            showNeedPermission()
        }
    }

    override fun createAdapter() = EntriesAdapter(this)

    override fun createPresenter(): PodcastsContract.Presenter {
        val appComponent: AppComponent = (requireActivity().application as App).appComponent
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
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                download()
            }
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                download()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> showPermissionRationale()

            else -> requestWritePermission()
        }
    }

    private fun download(){
        entryForDownload?.let {
            presenter.download(entryForDownload!!)
            entryForDownload = null
        }
    }

    private fun showNeedPermission() {
        Toast.makeText(context, getString(R.string.write_permission_rationale), Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.write_permission_rationale)
                .setPositiveButton("OK") { _, _ -> requestWritePermission() }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun requestWritePermission() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}