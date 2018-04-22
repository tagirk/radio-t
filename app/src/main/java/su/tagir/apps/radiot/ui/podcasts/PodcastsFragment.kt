package su.tagir.apps.radiot.ui.podcasts

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.widget.Toast
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.ListFragment
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import javax.inject.Inject

@RuntimePermissions
class PodcastsFragment : ListFragment<Entry>(), Injectable, EntriesAdapter.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var playerViewModel: PlayerViewModel

    private var entryForDownload: Entry? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        playerViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(PlayerViewModel::class.java)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        (listViewModel as PodcastsViewModel).startStatusTimer()
    }

    override fun onPause() {
        super.onPause()
        (listViewModel as PodcastsViewModel).stopStatusTimer()
    }

    override fun createViewModel() = ViewModelProviders.of(activity!!, viewModelFactory).get(PodcastsViewModel::class.java)

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PODCAST, GlideApp.with(this), this)

    override fun onClick(entry: Entry) {
        playerViewModel.onPlayClick(entry)
    }

    override fun download(entry: Entry) {
        entryForDownload = entry
        startDownloadWithPermissionCheck()
    }

    override fun openWebSite(entry: Entry) {
        (listViewModel as PodcastsViewModel).openWebSite(entry)
    }

    override fun openChatLog(entry: Entry) {
        (listViewModel as PodcastsViewModel).openChatLog(entry)
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownload(){
        if(entryForDownload!=null) {
            (listViewModel as PodcastsViewModel).onDownloadClick(entryForDownload!!)
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeedPermission(){
        Toast.makeText(context, "Для загрузки подкаста необходимо дать разрешение на запись.", Toast.LENGTH_SHORT).show()
    }

    override fun remove(entry: Entry) {
        (listViewModel as PodcastsViewModel).onRemoveClick(entry)

    }

    override fun onBackPressed() {
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun observeViewModel() {
        (listViewModel as PodcastsViewModel)
                .error
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            if (it != null) {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        })
    }
}