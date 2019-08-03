package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.ui.common.DataBoundListAdapter
import su.tagir.apps.radiot.utils.visibleGone

abstract class BaseMvpListFragment<M, V: MvpListView<M>, P: MvpPresenter<V>> : BaseMvpFragment<V, P>(), MvpListView<M>{

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

    protected lateinit var adapter: DataBoundListAdapter<M>

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View =
            inflater.inflate(R.layout.fragment_entry_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initList()
    }

    private fun initList() {
        adapter = createAdapter()
        list.adapter = adapter
        refreshLayout.setOnRefreshListener { loadData(pullToRefresh =  true) }

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()

                if (lastPosition == adapter.itemCount - 1) {
                    loadMore(lastIndex = lastPosition)
                }
            }
        })
    }

    @OnClick(R.id.btn_retry)
    fun retry() {
        loadData(pullToRefresh = false)
    }

    override fun updateState(viewState: ViewState<List<M>>) {
        showHideViews(viewState)
        viewState.data?.let { data ->
            adapter.replace(data)
        }
    }


    abstract fun createAdapter(): DataBoundListAdapter<M>

    override fun loadMore(lastIndex: Int){

    }

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