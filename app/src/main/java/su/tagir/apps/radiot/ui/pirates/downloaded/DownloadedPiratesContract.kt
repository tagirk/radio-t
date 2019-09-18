package su.tagir.apps.radiot.ui.pirates.downloaded

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface DownloadedPiratesContract{

    interface View: MvpListView<Entry>{
        fun showRemoveError(error: String)

    }

    interface Presenter: MvpListPresenter<Entry, View>, EntriesAdapter.Callback

}


