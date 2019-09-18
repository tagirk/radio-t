package su.tagir.apps.radiot.ui.search

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface SearchContract {

    interface View: MvpListView<Entry> {
        fun showDownloadError(error: String)
        fun showRecentQueries(queries: List<String>)
        fun download()
    }

    interface Presenter: MvpListPresenter<Entry, View>, EntriesAdapter.Callback{
        fun search(query: String)
        fun update()
        fun download()
        fun loadMore()
        fun removeQuery(query: String)
    }
}