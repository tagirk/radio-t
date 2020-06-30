package su.tagir.apps.radiot.ui.news

import android.os.Bundle
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.MvpListFragment

class NewsFragment : MvpListFragment<Entry, NewsContract.View, NewsContract.Presenter>(),
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
        val appComponent: AppComponent = (requireActivity().application as App).appComponent
        val categories = requireArguments().getStringArray("categories")!!.toList()
        return NewsPresenter(categories, appComponent.entryRepository, router = appComponent.router)
    }

    companion object{

        fun newInstance(categories: Array<String>): NewsFragment{
            val bundle = Bundle()
            bundle.putStringArray("categories", categories)
            val fr = NewsFragment()
            fr.arguments = bundle
            return fr
        }
    }

}