package su.tagir.apps.radiot.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.ListFragment
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject


class SearchFragment : ListFragment<Entry>(), EntriesAdapter.Callback, RecentQueriesAdapter.Callback, Injectable, ItemTouchHelper.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.search_view)
    lateinit var searchView: SearchView

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.recent_queries)
    lateinit var recentQueries: RecyclerView

    @BindView(R.id.layout_entries)
    lateinit var layoutEntries: View

    private lateinit var viewModel: SearchViewModel
//    private lateinit var adapter: SearchAdapter
    private lateinit var recentQueriesAdapter: RecentQueriesAdapter
    private val handler = Handler()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        adapter = SearchAdapter(null, GlideApp.with(this), this)
//        list.adapter = adapter

        recentQueriesAdapter = RecentQueriesAdapter(this)
        recentQueries.adapter = recentQueriesAdapter
        val itemTouchHelperCallback = ItemTouchHelper(this)
        android.support.v7.widget.helper.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recentQueries)

        refreshLayout.isEnabled = false
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = listViewModel as SearchViewModel

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    viewModel.search(newText)
                    layoutEntries.visibleGone(newText.isNotBlank())
                    recentQueries.visibleGone(newText.isBlank())
                }, 1000)

                return false
            }
        })



        observe()
        searchView.requestFocus()
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_search, container, false)

    override fun createViewModel(): ListViewModel<Entry>  = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)

    override fun createAdapter()= SearchAdapter(GlideApp.with(this), this)

    override val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(context)

    override fun onBackPressed() {
        viewModel.onBackClick()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startStatusTimer()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        viewModel.stopStatusTimer()
    }

    override fun onClick(entry: Entry) {
        viewModel.onEntryCkick(entry)
    }

    override fun onQueryClick(query: String?) {
        searchView.setQuery(query, true)
    }

    override fun download(entry: Entry) {
        viewModel.onDownloadClick(entry)
    }

    override fun remove(entry: Entry) {
        viewModel.onRemoveClick(entry)
    }

    override fun openWebSite(entry: Entry) {
        viewModel.openWebSite(entry)
    }

    override fun openChatLog(entry: Entry) {
        viewModel.openChatLog(entry)
    }


    override fun removeQuery(position: Int) {
        if (recentQueriesAdapter.currentList != null) {
            viewModel.removeQuery(recentQueriesAdapter.currentList!![position])
        }
    }

    private fun observe() {
        viewModel.getRecentSearches().observe(getViewLifecycleOwner()!!,
                Observer { queries -> recentQueriesAdapter.submitList(queries as PagedList<String>) })
    }


}