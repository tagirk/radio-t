package su.tagir.apps.radiot.ui.news

import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface NewsContract {

    interface View: MvpListView<Entry>{

    }

    interface Presenter: MvpListPresenter<Entry, View>, EntriesAdapter.Callback{
        fun observeNews()
    }

}