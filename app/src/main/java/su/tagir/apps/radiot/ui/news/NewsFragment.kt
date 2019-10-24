package su.tagir.apps.radiot.ui.news

import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment

class NewsFragment : BaseMvpListFragment<Entry, NewsContract.View, NewsContract.Presenter>(),
        NewsContract.View{


    override fun createAdapter() =
            EntriesAdapter(actionHandler = object : EntriesAdapter.Callback {
                        override fun select(entry: Entry) {
                            presenter.select(entry)
                        }

                        override fun download(entry: Entry) {}

                        override fun remove(entry: Entry) {}

                        override fun openComments(entry: Entry) {
                            presenter.openComments(entry)
                        }

                    })


    override fun createPresenter(): NewsContract.Presenter {
        val appComponent: AppComponent = (activity!!.application as App).appComponent
        return NewsPresenter(appComponent.entryRepository, router = appComponent.router)
    }


}