package su.tagir.apps.radiot.ui.comments

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import su.tagir.apps.radiot.model.entries.Node
import su.tagir.apps.radiot.model.repository.CommentsRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.Status
import su.tagir.apps.radiot.ui.mvp.ViewState
import su.tagir.apps.radiot.ui.viewmodel.ListViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class CommentsViewModel @Inject constructor(
        private val commentsRepository: CommentsRepository,
        scheduler: BaseSchedulerProvider) : ListViewModel<Node>(scheduler) {

    private var loadDisposable: Disposable? = null

    private val comments = LinkedList<Node>()

    private var postUrl: String? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            loadData()
        }

    fun setUrl(url: String?) {
        postUrl = url
    }

    override fun loadData() {
        loadDisposable?.dispose()
        loadDisposable = commentsRepository.getComments(postUrl ?: "")
                .observeOn(scheduler.ui())
                .doOnSubscribe { state.value = if (state.value == null) ViewState(Status.LOADING) else state.value?.copy(Status.LOADING) }
                .subscribe({
                    comments.clear()
                    comments.addAll(it.comments)
                    state.value = state.value?.copy(data = it.comments, status = Status.SUCCESS)
                },
                        {
                            Timber.e(it)
                            state.value = state.value?.copy(status = Status.ERROR)
                        })

        disposable += loadDisposable!!
    }

    override fun requestUpdates() {
        loadData()
    }

    fun showReplies(position: Int, node: Node) {
        val commentReplies = node.replies ?: return

        comments.removeAt(position)
        comments.add(position, node.copy(expanded = true))
        comments.addAll(position + 1, commentReplies.map { it.copy(level = node.level + 1) })
        state.value = state.value?.copy(data = comments.toList())
    }

    fun hideReplies(position: Int, node: Node) {
        Timber.d("hide: $position")
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
        state.value = state.value?.copy(data = comments.toList())
    }

}