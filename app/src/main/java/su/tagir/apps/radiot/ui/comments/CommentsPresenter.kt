package su.tagir.apps.radiot.ui.comments

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.model.repository.CommentsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import timber.log.Timber

class CommentsPresenter(private val postUrl: String,
                        private val commentsRepository: CommentsRepository,
                        private val router: Router,
                        private val scheduler: BaseSchedulerProvider) : BaseListPresenter<Node, CommentsContract.View>(), CommentsContract.Presenter {


    private var loadDisposable: Disposable? = null

    private val comments = mutableListOf<Node>()

    override fun doOnAttach(view: CommentsContract.View) {
        loadData(false)
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadDisposable?.dispose()
        loadDisposable = commentsRepository.getComments(postUrl)
                .observeOn(scheduler.ui())
                .doOnSubscribe { state = state.copy(Status.LOADING) }
                .subscribe({
                    comments.clear()
                    comments.addAll(it.comments)
                    state = state.copy(data = it.comments, status = Status.SUCCESS)
                },
                        {
                            Timber.e(it)
                            state = state.copy(status = Status.ERROR)
                        })

        disposables += loadDisposable!!
    }

    override fun showReplies(position: Int, node: Node) {
        val commentReplies = node.replies ?: return

        comments.removeAt(position)
        comments.add(position, node.copy(expanded = true))
        comments.addAll(position + 1, commentReplies.map { it.copy(level = node.level + 1) })
        state = state.copy(data = comments.toList())
    }

    override fun hideReplies(position: Int, node: Node) {
        comments.removeAt(position)
        val iterator = comments.listIterator(position)

        while (iterator.hasNext()) {
            val next = iterator.next()
            Timber.d("next: ${next.level}, node: ${node.level}")
            if (next.level > node.level) {
                iterator.remove()
                continue
            }
            break
        }
        comments.add(position, node.copy(expanded = false))
        state = state.copy(data = comments.toList())
    }

    override fun openUrl(url: String) {
        router.navigateTo(Screens.WebScreen(url))
    }
}