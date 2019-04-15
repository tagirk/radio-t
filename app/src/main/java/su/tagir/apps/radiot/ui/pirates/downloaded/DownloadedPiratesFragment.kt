package su.tagir.apps.radiot.ui.pirates.downloaded

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.PagedListFragment
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import javax.inject.Inject

class DownloadedPiratesFragment: PagedListFragment<Entry>(), EntriesAdapter.Callback, Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(PlayerViewModel::class.java)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
        observeViewModel()
    }

    override fun createViewModel()=ViewModelProviders.of(this, viewModelFactory).get(DownloadedPiratesViewModel::class.java)

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, GlideApp.with(this), this)

    override fun onClick(entry: Entry) {
        playerViewModel.onPlayClick(entry)
    }


    override fun openWebSite(entry: Entry) {
        mainViewModel.openWebSite(entry.url)
    }

    override fun openChatLog(entry: Entry) {
        mainViewModel.openWebSite(entry.chatUrl)
    }

    override fun onCommentsClick(entry: Entry) {
        mainViewModel.showComments(entry)
    }

    override fun remove(entry: Entry) {
        (listViewModel as DownloadedPiratesViewModel).onRemoveClick(entry)
    }

    override fun download(entry: Entry) {

    }

    private fun observeViewModel() {
        (listViewModel as DownloadedPiratesViewModel)
                .error()
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            if (it != null) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        })
    }
}