package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.databinding.FragmentEntryListBinding
import su.tagir.apps.radiot.ui.common.BindingListAdapter
import su.tagir.apps.radiot.utils.visibleGone

abstract class MvpListFragment<M, V: MvpListView<M>, P: MvpListPresenter<M, V>> : MvpFragment<V, P>(R.layout.fragment_entry_list), MvpListView<M>{

     val binding: FragmentEntryListBinding by viewBinding()

    protected lateinit var adapter: BindingListAdapter<M>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRetry.setOnClickListener { presenter.loadData(false) }

        initList()
    }

    private fun initList() {
        adapter = createAdapter()
        binding.list.adapter = adapter
        binding.refreshLayout.setOnRefreshListener { presenter.loadData(pullToRefresh =  true) }

        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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


    abstract fun createAdapter(): BindingListAdapter<M>

    protected open fun showHideViews(viewState: ViewState<List<M>>) {
        val isEmpty = (viewState.data?.size ?: 0) == 0

        binding.progress.visibleGone(viewState.loading && isEmpty)
        binding.textEmpty.visibleGone(viewState.completed && isEmpty)
        binding.textError.visibleGone(viewState.error && isEmpty)
        binding.btnRetry.visibleGone(viewState.error && isEmpty)
        binding.refreshLayout.visibleGone(viewState.completed || !isEmpty)
        binding.loadMoreProgress.visibleGone(viewState.loadingMore)
        binding.refreshLayout.isRefreshing = viewState.refreshing
    }
}