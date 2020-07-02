package su.tagir.apps.radiot.ui.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.databinding.FragmentSearchBinding
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.common.autoCleared
import su.tagir.apps.radiot.ui.mvp.MvpFragment
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.utils.visibleGone

class SearchFragment :
        MvpFragment<SearchContract.View, SearchContract.Presenter>(R.layout.fragment_search),
        SearchContract.View,
        RecentQueriesAdapter.Callback,
        ItemTouchHelper.Callback,
        EntriesAdapter.Callback {

    private val searchBinding: FragmentSearchBinding by viewBinding()

    private var recentQueriesAdapter by autoCleared<RecentQueriesAdapter>()
    private var adapter by autoCleared<SearchAdapter>()

    private val handler = Handler()

    private var searchView: SearchView? = null

    private var entryForDownload: Entry? = null
        set(value) {
            field = value
            value?.let {
                startDownload()
            }
        }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
        if(isGranted){
            startDownload()
        }else{
            showNeedPermission()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { presenter.exit() }
        toolbar.inflateMenu(R.menu.menu_search)
        initSearchView(toolbar.menu)

        adapter = SearchAdapter(this)
        searchBinding.layoutEntries.list.adapter = adapter

        recentQueriesAdapter = RecentQueriesAdapter(this)
        searchBinding.recentQueries.adapter = recentQueriesAdapter
        val itemTouchHelperCallback = ItemTouchHelper(this)
        androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(searchBinding.recentQueries)

        searchBinding.layoutEntries.refreshLayout.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        entryForDownload?.let {
            presenter.download(entryForDownload!!)
            entryForDownload = null
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    override fun updateState(viewState: ViewState<List<Entry>>) {
        showHideViews(viewState)
        viewState.data?.let { data ->
            adapter.replace(data)
        }
    }

    override fun onQueryClick(query: String?) {
        searchView?.setQuery(query, false)
    }

    override fun removeQuery(position: Int) {
        recentQueriesAdapter.items.let { list ->

            list[position].let { query ->
                presenter.removeQuery(query)
            }
        }
    }

    override fun createPresenter(): SearchContract.Presenter {
        val appComponent: AppComponent = (requireActivity().application as App).appComponent
        return SearchPresenter(appComponent.entryRepository, appComponent.router)
    }

    override fun showDownloadError(error: String?) {
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
        recentQueriesAdapter.replace(queries)
    }

    override fun select(entry: Entry) {
        presenter.download(entry)
    }

    override fun download(entry: Entry) {
        entryForDownload = entry
    }

    override fun remove(entry: Entry) {
        presenter.remove(entry)
    }

    override fun openComments(entry: Entry) {
        presenter.openComments(entry)
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
                    searchBinding.layoutEntries.getRoot().visibleGone(!newText.isNullOrBlank())
                    searchBinding.recentQueries.visibleGone(newText.isNullOrBlank())
                }, 1000)

                return false
            }

        })
    }

    private fun startDownload() {
        if (context == null) {
            return
        }
        if (entryForDownload == null) {
            return
        }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                download()
            }
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ->{
                entryForDownload?.let {
                    presenter.download(entryForDownload!!)
                    entryForDownload = null
                }
            }

            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> showPermissionRationale()

            else -> requestWritePermission()
        }
    }

    private fun download(){
        entryForDownload?.let {
            presenter.download(entryForDownload!!)
            entryForDownload = null
        }
    }

    private fun showNeedPermission() {
        Toast.makeText(context, getString(R.string.write_permission_rationale), Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
                .setMessage(R.string.write_permission_rationale)
                .setPositiveButton("OK"){_, _ -> requestWritePermission()}
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun requestWritePermission(){
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun showHideViews(viewState: ViewState<List<Entry>>) {
        val isEmpty = (viewState.data?.size ?: 0) == 0

        searchBinding.layoutEntries.progress.visibleGone(viewState.loading && isEmpty)
        searchBinding.layoutEntries.textEmpty.visibleGone(viewState.completed && isEmpty)
        searchBinding.layoutEntries.textError.visibleGone(viewState.error && isEmpty)
        searchBinding.layoutEntries.btnRetry.visibleGone(viewState.error && isEmpty)
        searchBinding.layoutEntries.refreshLayout.visibleGone(viewState.completed || !isEmpty)
        searchBinding.layoutEntries.loadMoreProgress.visibleGone(viewState.loadingMore)
        searchBinding.layoutEntries.refreshLayout.isRefreshing = viewState.refreshing
    }
}