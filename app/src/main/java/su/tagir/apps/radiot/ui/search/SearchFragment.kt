package su.tagir.apps.radiot.ui.search

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.REQUEST_WRITE_PERMISSION
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.FragmentsInteractionListener
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.utils.visibleGone

class SearchFragment :
        BaseMvpListFragment<Entry, SearchContract.View, SearchContract.Presenter>(),
        SearchContract.View,
        RecentQueriesAdapter.Callback,
        ItemTouchHelper.Callback,
        EntriesAdapter.Callback {

    private lateinit var recentQueries: RecyclerView

    private lateinit var layoutEntries: View

    private lateinit var recentQueriesAdapter: RecentQueriesAdapter
    private val handler = Handler()

    private var searchView: SearchView? = null

    private var entryForDownload: Entry? = null
        set(value) {
            field = value
            value?.let {
                startDownload()
            }
        }

    private var interactionListener: FragmentsInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionListener = context as FragmentsInteractionListener
    }

    override fun onDetach() {
        interactionListener = null
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        interactionListener?.lockDrawer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentQueries = view.findViewById(R.id.recent_queries)
        layoutEntries = view.findViewById(R.id.layout_entries)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { presenter.exit() }
        toolbar.inflateMenu(R.menu.menu_search)
        initSearchView(toolbar.menu)

        recentQueriesAdapter = RecentQueriesAdapter(this)
        recentQueries.adapter = recentQueriesAdapter
        val itemTouchHelperCallback = ItemTouchHelper(this)
        androidx.recyclerview.widget.ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recentQueries)

        refreshLayout.isEnabled = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_search, container, false)

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

    override fun createAdapter(): DataBoundListAdapter<Entry> {
        return SearchAdapter( this)
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
        val appComponent: AppComponent = (activity!!.application as App).appComponent
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode != REQUEST_WRITE_PERMISSION){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startDownload()
        }else{
            showNeedPermission()
        }
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
                    layoutEntries.visibleGone(!newText.isNullOrBlank())
                    recentQueries.visibleGone(newText.isNullOrBlank())
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
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> presenter.download(entryForDownload!!)

            ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) -> showPermissionRationale()

            else -> requestWritePermission()
        }
    }

    private fun showNeedPermission() {
        Toast.makeText(context, getString(R.string.write_permission_rationale), Toast.LENGTH_SHORT).show()
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(context!!)
                .setMessage(R.string.write_permission_rationale)
                .setPositiveButton("OK"){_, _ -> requestWritePermission()}
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
    }

    private fun requestWritePermission(){
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
    }
}