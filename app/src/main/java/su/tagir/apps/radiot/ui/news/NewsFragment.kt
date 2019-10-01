package su.tagir.apps.radiot.ui.news

import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpListFragment
import javax.inject.Inject

class NewsFragment : BaseMvpListFragment<Entry, NewsContract.View, NewsContract.Presenter>(),
        NewsContract.View,
        Injectable {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var entryRepository: EntryRepository

    override fun createAdapter() =
            EntriesAdapter(type = EntriesAdapter.TYPE_NEWS,
                    glide = GlideApp.with(this),
                    actionHandler = object : EntriesAdapter.Callback {
                        override fun select(entry: Entry) {
                            presenter.select(entry)
                        }

                        override fun download(entry: Entry) {}

                        override fun remove(entry: Entry) {}

                        override fun openComments(entry: Entry) {
                            presenter.openComments(entry)
                        }

                    })


    override fun createPresenter(): NewsContract.Presenter =
            NewsPresenter(entryRepository, router =  router)


}