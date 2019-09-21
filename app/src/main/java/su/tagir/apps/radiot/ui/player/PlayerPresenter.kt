package su.tagir.apps.radiot.ui.player

import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PlayerPresenter(private val entryRepository: EntryRepository,
                      private val scheduler: BaseSchedulerProvider,
                      private val router: Router) : BasePresenter<PlayerContract.View>(), PlayerContract.Presenter {

    private var listener: PlayerContract.Presenter.InteractionListener? = null

    private var currentPodcast: Entry? = null
        set(value) {
            field = value
            listener?.showCurrent(value)
            value?.let { entry ->
                view?.showCurrentPodcast(entry)

            }
        }

    override fun setListener(listener: PlayerContract.Presenter.InteractionListener) {
        this.listener = listener
    }

    override fun doOnAttach(view: PlayerContract.View) {
        observeCurrent()
        startUpdateProgress()
    }

    override fun doOnDetach() {
        listener = null
    }

    override fun pause() {
        entryRepository.pause()
    }

    override fun resume() {
        entryRepository.resume()
    }

    override fun playStream() {
        entryRepository.playStream(STREAM_URL)
    }

    override fun onArticleClick(article: Article?) {
        article?.link?.let { link ->
            router.navigateTo(Screens.WebScreen(link))
        }
    }

    override fun showChat() {
        currentPodcast?.let { entry ->

            if (entry.url != STREAM_URL) {
                router.navigateTo(Screens.WebScreen(entry.chatUrl))
            } else {
                router.navigateTo(Screens.ChatScreen)
            }
        }
    }

    override fun openWebPage() {
        currentPodcast?.url?.let { url ->
            router.navigateTo(Screens.WebScreen(url))
        }
    }

    override fun onTitleClick() {
        listener?.onExpand()
    }

    override fun seekTo(timeLabel: TimeLabel) {
        timeLabel.time?.let{time ->
            view?.seekTo(time)
        }
    }

    private fun observeCurrent() {
        disposables += entryRepository.getCurrent()
                .observeOn(scheduler.ui())
                .doOnNext { entry -> currentPodcast = entry }
                .flatMap { entry -> entryRepository.getTimeLabels(entry) }
                .observeOn(scheduler.ui())
                .subscribe({ labels ->
                    view?.showTimeLabels(labels)
                }, {
                    Timber.e(it)
                })
    }

    private fun startUpdateProgress() {
        disposables += Observable
                .interval(0L, 1L, TimeUnit.SECONDS)
                .observeOn(scheduler.ui())
                .subscribe({ view?.requestProgress() },
                        { Timber.d(it) })
    }
}