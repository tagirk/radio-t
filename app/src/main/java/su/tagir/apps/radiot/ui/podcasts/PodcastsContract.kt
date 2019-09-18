package su.tagir.apps.radiot.ui.podcasts

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface PodcastsContract {

    interface View: MvpListView<Entry>{
        fun showDownloadError(error: String)
        fun download()
    }

    interface Presenter: MvpListPresenter<Entry, View>, EntriesAdapter.Callback{

        fun download()

    }
}