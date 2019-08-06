package su.tagir.apps.radiot.ui.news

import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.ui.mvp.MvpListPresenter
import su.tagir.apps.radiot.ui.mvp.MvpListView

interface ArticlesContract{

    interface View: MvpListView<Article>{

    }

    interface Presenter: MvpListPresenter<Article, View>, ArticlesAdapter.Callback{
        fun updateActiveTheme()
    }
}