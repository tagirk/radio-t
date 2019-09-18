package su.tagir.apps.radiot.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.Observable
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.utils.visibleGone

@RuntimePermissions
class SearchFragment:
        BaseMvpListFragment<Entry, SearchContract.View, SearchContract.Presenter>(),
        SearchContract.View,
        RecentQueriesAdapter.Callback,
        Injectable,
        ItemTouchHelper.Callback{

    private lateinit var recentQueries: RecyclerView

    private lateinit var layoutEntries: View

    private lateinit var recentQueriesAdapter: RecentQueriesAdapter
    private val handler = Handler()

    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentQueries = view.findViewById(R.id.recent_queries)
        layoutEntries = view.findViewById(R.id.layout_entries)

        recentQueriesAdapter = RecentQueriesAdapter(this)
        recentQueries.adapter = recentQueriesAdapter
        val itemTouchHelperCallback = ItemTouchHelper(this)
        androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recentQueries)

        refreshLayout.isEnabled = false
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_search, container, false)

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        initSearchView(menu)

    }

    override fun updateState(viewState: ViewState<List<Entry>>) {
        showHideViews(viewState)
        viewState.data?.let { data ->
            adapter.replace(data)
        }
    }

    override fun loadData(pullToRefresh: Boolean) {
        presenter.update()
    }

    override fun createAdapter(): DataBoundListAdapter<Entry> {
        return SearchAdapter(Glide.with(this))
    }

    override fun onQueryClick(query: String?) {
       searchView?.setQuery(query, false)
    }

    override fun removeQuery(position: Int) {
        recentQueriesAdapter.currentList?.let { list ->

            list[position]?.let { query ->
                presenter.removeQuery(query)
            }
        }
    }

    override fun createPresenter(): SearchContract.Presenter {
       throw UnsupportedOperationException()
    }

    override fun showDownloadError(error: String) {
        context?.let { c ->
            AlertDialog.Builder(c)
                    .setTitle(R.string.error)
                    .setMessage(error)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    override fun showRecentQueries(queries: List<String>) {
        recentQueriesAdapter.submitList(queries as PagedList<String>)
    }

    override fun download() {
        startDownloadWithPermissionCheck()
    }

    override fun entryClickRequests(): Observable<Entry> = (adapter as SearchAdapter).entryClicks()

    override fun downloadClickRequests(): Observable<Entry> = (adapter as SearchAdapter).downloadClicks()

    override fun removeClickRequests(): Observable<Entry> = (adapter as SearchAdapter).removeClicks()

    override fun commentClickRequests(): Observable<Entry> = (adapter as SearchAdapter).commentClicks()

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownload() {
        presenter.download()

    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeedPermission() {
        Toast.makeText(context, "Для загрузки подкаста необходимо дать разрешение на запись.", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }


    private fun initSearchView(menu: Menu?) {
        searchView = menu?.findItem(R.id.search_view)?.actionView as SearchView?
        searchView?.setIconifiedByDefault(false)
        searchView?.maxWidth = Int.MAX_VALUE
        val magImage = searchView?.findViewById(R.id.search_mag_icon) as ImageView?
        magImage?.visibility = View.GONE
        magImage?.setImageDrawable(null)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    presenter.search(newText ?: "")
                    layoutEntries.visibleGone(!newText.isNullOrBlank())
                    recentQueries.visibleGone(newText.isNullOrBlank())
                }, 1000)

                return false
            }

        })
    }
}