package su.tagir.apps.radiot.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import butterknife.BindView
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject


class SearchFragment : BaseFragment(), EntriesAdapter.Callback, RecentQueriesAdapter.Callback, Injectable, ItemTouchHelper.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @BindView(R.id.progress)
    lateinit var progress: View

    @BindView(R.id.btn_retry)
    lateinit var btnRetry: View

    @BindView(R.id.text_error)
    lateinit var errorText: View

    @BindView(R.id.text_empty)
    lateinit var emptyView: View

    @BindView(R.id.refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout

    @BindView(R.id.list)
    lateinit var list: RecyclerView

    @BindView(R.id.load_more_progress)
    lateinit var loadMoreProgress: ProgressBar

    @BindView(R.id.search_view)
    lateinit var searchView: SearchView

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.recent_queries)
    lateinit var recentQueries: RecyclerView

    @BindView(R.id.layout_entries)
    lateinit var layoutEntries: View

    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: SearchAdapter
    private lateinit var recentQueriesAdapter: RecentQueriesAdapter
    private val handler = Handler()

    private var itemsCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SearchAdapter(null, GlideApp.with(this), this)
        list.adapter = adapter

        recentQueriesAdapter = RecentQueriesAdapter(this)
        recentQueries.adapter = recentQueriesAdapter
        val itemTouchHelperCallback = ItemTouchHelper(this)
        android.support.v7.widget.helper.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recentQueries)

        refreshLayout.isEnabled = false
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query)
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

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager
                        .findLastVisibleItemPosition()
                if (lastPosition == adapter.itemCount - 1) {
                    viewModel.searchMore()
                }
            }
        })

        observe()
        searchView.requestFocus()
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_search, container, false)

    override fun onBackPressed() {
        viewModel.onBackClick()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        viewModel.onPause()
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
        viewModel.data
                .observe(getViewLifecycleOwner()!!,
                        Observer { entries ->
                            adapter.replace(entries)
                            itemsCount = entries?.size ?: 0
                            showHideViews(viewModel.state.value)
                        })

        viewModel.state
                .observe(getViewLifecycleOwner()!!,
                        Observer { state -> showHideViews(state) })

        viewModel.resentSearches.observe(getViewLifecycleOwner()!!,
                Observer { queries -> recentQueriesAdapter.setList(queries) })
    }

    private fun showHideViews(state: ViewModelState?) {
        if (state == null) {
            return
        }
        val isEmpty = itemsCount == 0
        progress.visibleGone(state.loading)
        emptyView.visibleGone(state.isCompleted() && isEmpty)
        errorText.visibleGone(state.error && isEmpty)
        btnRetry.visibleGone(state.error && isEmpty)
        loadMoreProgress.visibleGone(state.loadingMore)
        list.visibleGone(!state.loading)
    }
}