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
import android.view.*
import android.widget.ImageView
import butterknife.BindView
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.MainViewModel
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.ListFragment
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.utils.visibleGone
import javax.inject.Inject

class SearchFragment : ListFragment<Entry>(), EntriesAdapter.Callback, RecentQueriesAdapter.Callback,
        Injectable, ItemTouchHelper.Callback{

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    @BindView(R.id.recent_queries)
    lateinit var recentQueries: RecyclerView

    @BindView(R.id.layout_entries)
    lateinit var layoutEntries: View

    private lateinit var viewModel: SearchViewModel
    private lateinit var recentQueriesAdapter: RecentQueriesAdapter
    private val handler = Handler()

    private lateinit var mainViewModel: MainViewModel
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentQueriesAdapter = RecentQueriesAdapter(this)
        recentQueries.adapter = recentQueriesAdapter
        val itemTouchHelperCallback = ItemTouchHelper(this)
        android.support.v7.widget.helper.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recentQueries)

        refreshLayout.isEnabled = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = listViewModel as SearchViewModel
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
        observe()
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_search, container, false)

    override fun createViewModel(): ListViewModel<Entry> = ViewModelProviders.of(activity!!, viewModelFactory).get(SearchViewModel::class.java)

    override fun createAdapter() = SearchAdapter(GlideApp.with(this), this)

    override val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(context)


    override fun onResume() {
        super.onResume()
        mainViewModel.setCurrentScreen(Screens.SEARCH_SCREEN)
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        viewModel.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_search, menu)
        initSearchView(menu)

    }

    override fun onClick(entry: Entry) {
        if (entry.audioUrl != null) {
            viewModel.onEntryClick(entry)
        } else {
            mainViewModel.openWebSite(entry.url)
        }
    }

    override fun onQueryClick(query: String?) {
       searchView?.setQuery(query, false)
    }

    override fun download(entry: Entry) {
        viewModel.onDownloadClick(entry)
    }

    override fun remove(entry: Entry) {
        viewModel.onRemoveClick(entry)
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
    override fun removeQuery(position: Int) {
        if (recentQueriesAdapter.currentList != null) {
            viewModel.removeQuery(recentQueriesAdapter.currentList!![position])
        }
    }

    private fun observe() {
        viewModel.getRecentSearches().observe(getViewLifecycleOwner()!!,
                Observer { queries -> recentQueriesAdapter.submitList(queries as PagedList<String>) })

    }

    private fun initSearchView(menu: Menu?) {
        searchView = menu?.findItem(R.id.search_view)?.actionView as SearchView?
        searchView?.setIconifiedByDefault(false)
        searchView?.maxWidth = Int.MAX_VALUE
        val magImage = searchView?.findViewById(android.support.v7.appcompat.R.id.search_mag_icon) as ImageView?
        magImage?.visibility = View.GONE
        magImage?.setImageDrawable(null)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    viewModel.search(newText ?: "")
                    layoutEntries.visibleGone(!newText.isNullOrBlank())
                    recentQueries.visibleGone(newText.isNullOrBlank())
                }, 1000)

                return false
            }

        })
    }
}