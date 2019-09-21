package su.tagir.apps.radiot.ui.news

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.GlideApp
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.EntriesAdapter
import su.tagir.apps.radiot.ui.mvp.BaseMvpPagedListFragment
import javax.inject.Inject

class NewsFragment : BaseMvpPagedListFragment<Entry, NewsContract.View, NewsContract.Presenter>(),
        NewsContract.View,
        Injectable {

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var scheduler: BaseSchedulerProvider

    @Inject
    lateinit var entryRepository: EntryRepository

    override fun createAdapter(): PagedListAdapter<Entry, out RecyclerView.ViewHolder> =
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
            NewsPresenter(entryRepository, scheduler, router)


}