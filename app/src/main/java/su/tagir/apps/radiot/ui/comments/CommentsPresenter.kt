package su.tagir.apps.radiot.ui.comments

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.model.repository.CommentsRepository
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBaseListPresenter
import su.tagir.apps.radiot.ui.mvp.Status
import timber.log.Timber

class CommentsPresenter(private val postUrl: String,
                        private val commentsRepository: CommentsRepository,
                        private val router: Router,
                        dispatcher: CoroutineDispatcher = MainDispatcher()) : MvpBaseListPresenter<Node, CommentsContract.View>(dispatcher), CommentsContract.Presenter {


    private var loadJob: Job? = null

    private val comments = mutableListOf<Node>()

    override fun doOnAttach(view: CommentsContract.View) {
        loadData(false)
    }

    override fun doOnDetach() {
        loadJob?.cancel()
    }

    override fun loadData(pullToRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = launch{
            state = state.copy(Status.LOADING)
            val tree = commentsRepository.getComments(postUrl)
            comments.clear()
            comments.addAll(tree.comments)
            state = state.copy(data = tree.comments, status = Status.SUCCESS)
        }
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