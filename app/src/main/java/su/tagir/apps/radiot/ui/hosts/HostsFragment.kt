package su.tagir.apps.radiot.ui.hosts

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Host
import su.tagir.apps.radiot.ui.common.PagedListFragment
import javax.inject.Inject


class HostsFragment : PagedListFragment<Host>(), Injectable, HostsAdapter.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun createViewModel() = ViewModelProviders.of(activity!!, viewModelFactory).get(HostsViewModel::class.java)

    override fun createAdapter() = HostsAdapter(GlideApp.with(this), this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.isEnabled = false
    }

    override fun onSocialNetClick(url: String) {
        (listViewModel as HostsViewModel).openSocialNet(url)
    }
}