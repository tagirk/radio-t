package su.tagir.apps.radiot.ui.common

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.OnClick
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import su.tagir.apps.radiot.ui.viewmodel.ViewModelState
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

    protected lateinit var listViewModel: ListViewModel<T>

    private lateinit var adapter: PagedListAdapter<T, out RecyclerView.ViewHolder>

    private var itemsCount = 0

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
        refreshLayout.setOnRefreshListener { listViewModel.update(true) }

        listViewModel
                .state
                .observe(getViewLifecycleOwner()!!, Observer { t ->
                    if (t != null) {
                        showHideViews(t, itemsCount)
                    }
                })

        listViewModel
                .getData()
                .observe(getViewLifecycleOwner()!!,
                        Observer<PagedList<T>> { t ->
                            itemsCount = t?.size ?: 0
                            showHideViews(listViewModel.state.value, itemsCount)
                            adapter.setList(t)

                        }
                )
    }

    @OnClick(R.id.btn_retry)
    fun retry() {
        listViewModel.update(false)
    }

    override fun onBackPressed() {

    }

    abstract fun createViewModel(): ListViewModel<T>

    abstract fun createAdapter(): PagedListAdapter<T, out RecyclerView.ViewHolder>

    private fun showHideViews(state: ViewModelState?, itemsCount:Int) {
        if (state == null) {
            return
        }
        val isEmpty = itemsCount == 0
        progress.visibleGone(state.loading && isEmpty)
        emptyView.visibleGone(state.isCompleted() && isEmpty)
        errorText.visibleGone(state.error && isEmpty)
        btnRetry.visibleGone(state.error && isEmpty)
        refreshLayout.visibleGone(state.isCompleted() || itemsCount > 0)
        refreshLayout.isRefreshing = state.refreshing
    }
}