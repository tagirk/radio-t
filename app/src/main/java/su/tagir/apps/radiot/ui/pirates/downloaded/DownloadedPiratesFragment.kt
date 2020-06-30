package su.tagir.apps.radiot.ui.pirates.downloaded

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListFragment

class DownloadedPiratesFragment :
        MvpListFragment<Entry, DownloadedPiratesContract.View, DownloadedPiratesContract.Presenter>(),
        DownloadedPiratesContract.View,
        EntriesAdapter.Callback {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refreshLayout.isEnabled = false
    }


    override fun createAdapter() = EntriesAdapter(this)

    override fun createPresenter(): DownloadedPiratesContract.Presenter {
        val appComponent: AppComponent = (requireActivity().application as App).appComponent
        return DownloadedPiratesPresenter(appComponent.entryRepository)
    }

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

    override fun download(entry: Entry) {

    }

    override fun remove(entry: Entry) {
        presenter.remove(entry)
    }

    override fun openComments(entry: Entry) {

    }
}