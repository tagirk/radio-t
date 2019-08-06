package su.tagir.apps.radiot.ui.pirates

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListView
import su.tagir.apps.radiot.ui.mvp.MvpPresenter

interface PiratesContract{

    interface View: MvpListView<Entry>{
        fun download()
        fun showDownloadError(error: String)
    }

    interface Presenter: MvpPresenter<View>, EntriesAdapter.Callback{
        fun download()
    }

}


