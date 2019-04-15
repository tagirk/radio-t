package su.tagir.apps.radiot.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import su.tagir.apps.radiot.utils.visibleGone

abstract class PagedListFragment<T> : BaseFragment() {

    @BindView(R.id.progress)
    lateinit var progress: View

    @BindView(R.id.btn_retry)
    lateinit var btnRetry: View

    @BindView(R.id.text_error)
    lateinit var errorText: View

    @BindView(R.id.text_empty)
    lateinit var emptyView: View

    @BindView(R.id.list)
    lateinit var list: RecyclerView

    @BindView(R.id.refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout

    @BindView(R.id.load_more_progress)
    lateinit var loadMoreProgress: ProgressBar

    protected lateinit var listViewModel: ListViewModel<T>

    private lateinit var adapter: PagedListAdapter<T, out RecyclerView.ViewHolder>

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_entry_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listViewModel = createViewModel()
        initList()
    }

    private fun initList() {
        adapter = createAdapter()
        list.adapter = adapter
        refreshLayout.setOnRefreshListener { listViewModel.update() }

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()

                val status = listViewModel.state().value?.status

                if (lastPosition == adapter.itemCount - 1
                        && status != Status.LOADING
                        && status != Status.LOADING_MORE
                        && status != Status.REFRESHING
                        && listViewModel.state().value?.hasNextPage == true) {

                    listViewModel.loadMore()
                }
            }
        })

        listViewModel
                .state()
                .observe(getViewLifecycleOwner()!!, Observer { t ->
                    if (t != null) {
                        showHideViews(t)
                    }
                })

        listViewModel
                .state()
                .observe(getViewLifecycleOwner()!!, Observer { t ->
                    showHideViews(t)
                    adapter.submitList(t?.data as PagedList<T>?)
                })
    }

    @OnClick(R.id.btn_retry)
    fun retry() {
        listViewModel.update()
    }

    abstract fun createViewModel(): ListViewModel<T>

    abstract fun createAdapter(): PagedListAdapter<T, out RecyclerView.ViewHolder>

    protected open fun showHideViews(state: State<List<T>>?) {
        if (state == null) {
            return
        }
        val isEmpty = (state.data?.size ?: 0) == 0

        progress.visibleGone(state.loading && isEmpty)
        emptyView.visibleGone(state.completed && isEmpty)
        errorText.visibleGone(state.error && isEmpty)
        btnRetry.visibleGone(state.error && isEmpty)
        refreshLayout.visibleGone(state.completed || !isEmpty)
        loadMoreProgress.visibleGone(state.loadingMore)
        refreshLayout.isRefreshing = state.refreshing
    }
}