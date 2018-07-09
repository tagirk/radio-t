package su.tagir.apps.radiot.ui.pirates

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
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.PagedListFragment
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import javax.inject.Inject

@RuntimePermissions
class PiratesFragment :PagedListFragment<Entry>(), Injectable, EntriesAdapter.Callback {

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
        (listViewModel as PiratesViewModel).startStatusTimer()
    }

    override fun onPause() {
        super.onPause()
        (listViewModel as PiratesViewModel).stopStatusTimer()
    }

    override fun createViewModel() = ViewModelProviders.of(this, viewModelFactory).get(PiratesViewModel::class.java)

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_PIRATES, GlideApp.with(this), this)

    override fun onClick(entry: Entry) {
        playerViewModel.onPlayClick(entry)
    }

    override fun download(entry: Entry) {
        entryForDownload = entry
        startDownloadWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownload(){
        if(entryForDownload!=null) {
            (listViewModel as PiratesViewModel).onDownloadClick(entryForDownload!!)
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeedPermission(){
        Toast.makeText(context, "Для загрузки подкаста необходимо дать разрешение на запись.", Toast.LENGTH_SHORT).show()
    }

    override fun remove(entry: Entry) {
        (listViewModel as PiratesViewModel).onRemoveClick(entry)
    }

    override fun openWebSite(entry: Entry) {
//        (listViewModel as PodcastsViewModel).openWebSite(entry)
    }

    override fun openChatLog(entry: Entry) {
//        (listViewModel as PodcastsViewModel).openChatLog(entry)
    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun observeViewModel() {
        (listViewModel as PiratesViewModel)
                .getDownloadError()
                .observe(getViewLifecycleOwner()!!,
                        Observer {
                            if (it != null) {
                                showToast(it)
                            }
                        })
    }

}