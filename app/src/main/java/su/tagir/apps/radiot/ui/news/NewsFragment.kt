package su.tagir.apps.radiot.ui.news

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.PagedListFragment
import javax.inject.Inject

class NewsFragment : PagedListFragment<Entry>(), Injectable, EntriesAdapter.Callback {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun createViewModel() = ViewModelProviders.of(this, viewModelFactory).get(NewsViewModel::class.java)

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_NEWS, null, this)

    override fun onClick(entry: Entry) {
        (listViewModel as NewsViewModel).onEntryClick(entry)
    }

    override fun download(entry: Entry) {}

    override fun remove(entry: Entry) {}

    override fun openWebSite(entry: Entry) {
    }

    override fun openChatLog(entry: Entry) {
    }
}