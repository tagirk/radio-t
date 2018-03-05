package su.tagir.apps.radiot.ui.news

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.ListFragment
import javax.inject.Inject

class NewsFragment : ListFragment<Entry>(), Injectable, EntriesAdapter.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun createViewModel() = ViewModelProviders.of(activity!!, viewModelFactory).get(NewsViewModel::class.java)

    override fun createAdapter() = EntriesAdapter(EntriesAdapter.TYPE_NEWS, null, this)

    override fun onClick(entry: Entry) {
        (listViewModel as NewsViewModel).onEntryClick(entry)
    }

    override fun download(entry: Entry) {}

    override fun remove(entry: Entry) {}


}