package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.utils.visibleGone

abstract class BaseMvpListFragment<M, V: MvpListView<M>, P: MvpListPresenter<M, V>> : BaseMvpFragment<V, P>(), MvpListView<M>{

    lateinit var progress: View

    lateinit var btnRetry: View

    lateinit var errorText: View

    lateinit var emptyView: View

    lateinit var list: RecyclerView

    lateinit var refreshLayout: SwipeRefreshLayout

    lateinit var loadMoreProgress: ProgressBar

    protected lateinit var adapter: DataBoundListAdapter<M>

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_entry_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = view.findViewById(R.id.progress)
        btnRetry = view.findViewById(R.id.btn_retry)
        errorText = view.findViewById(R.id.text_error)
        emptyView = view.findViewById(R.id.text_empty)
        list = view.findViewById(R.id.list)
        refreshLayout = view.findViewById(R.id.refresh_layout)
        loadMoreProgress = view.findViewById(R.id.load_more_progress)

        btnRetry.setOnClickListener { presenter.loadData(false) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initList()
    }

    private fun initList() {
        adapter = createAdapter()
        list.adapter = adapter
        refreshLayout.setOnRefreshListener { presenter.loadData(pullToRefresh =  true) }

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()

                if (lastPosition == adapter.itemCount - 1) {
                    presenter.loadMore(lastIndex = lastPosition)
                }
            }
        })
    }

    override fun updateState(viewState: ViewState<List<M>>) {
        showHideViews(viewState)
        viewState.data?.let { data ->
            adapter.replace(data)
        }
    }


    abstract fun createAdapter(): DataBoundListAdapter<M>

    protected open fun showHideViews(viewState: ViewState<List<M>>) {
        val isEmpty = (viewState.data?.size ?: 0) == 0

        progress.visibleGone(viewState.loading && isEmpty)
        emptyView.visibleGone(viewState.completed && isEmpty)
        errorText.visibleGone(viewState.error && isEmpty)
        btnRetry.visibleGone(viewState.error && isEmpty)
        refreshLayout.visibleGone(viewState.completed || !isEmpty)
        loadMoreProgress.visibleGone(viewState.loadingMore)
        refreshLayout.isRefreshing = viewState.refreshing
    }
}