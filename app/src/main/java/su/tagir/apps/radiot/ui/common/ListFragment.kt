package su.tagir.apps.radiot.ui.common

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import butterknife.BindView
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.State
import su.tagir.apps.radiot.ui.viewmodel.Status
import su.tagir.apps.radiot.utils.visibleGone

abstract class ListFragment<T> : BaseFragment() {

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

    private lateinit var adapter: DataBoundListAdapter<T>

    protected lateinit var listViewModel: ListViewModel<T>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnRetry.setOnClickListener { listViewModel.update() }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listViewModel = createViewModel()


        initList()
    }


    private fun initList() {
        adapter = createAdapter()
        list.adapter = adapter
        list.layoutManager = layoutManager
        refreshLayout.setOnRefreshListener { listViewModel.update() }
        btnRetry.setOnClickListener { listViewModel.loadData() }
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager
                        .findLastVisibleItemPosition()
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
                    showHideViews(t)
                    adapter.replace(t?.data)
                })

    }

    abstract fun createViewModel(): ListViewModel<T>

    abstract fun createAdapter(): DataBoundListAdapter<T>

    abstract val layoutManager: RecyclerView.LayoutManager

    private fun showHideViews(state: State<List<T>>?) {
        if (state == null) {
            return
        }
        val isEmpty = (state.data?.size ?: 0) == 0
        progress.visibleGone(state.loading && isEmpty)
        emptyView.visibleGone(state.completed && isEmpty)
        errorText.visibleGone(state.error && isEmpty)
        btnRetry.visibleGone(state.error && isEmpty)
        refreshLayout.isRefreshing = state.refreshing
        loadMoreProgress.visibleGone(state.loadingMore)

        val message = state.getErrorIfNotHandled()
        if (message != null) {
            showToast(message)
        }
    }
}