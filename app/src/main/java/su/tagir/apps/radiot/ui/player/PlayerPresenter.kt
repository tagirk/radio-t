package su.tagir.apps.radiot.ui.player

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.utils.startTimer

class PlayerPresenter(private val entryRepository: EntryRepository,
                      private val router: Router,
                      dispatcher: CoroutineDispatcher = MainDispatcher()) : BasePresenter<PlayerContract.View>(dispatcher), PlayerContract.Presenter {

    private var currentPodcast: Entry? = null
        set(value) {
            field = value
            value?.let { entry ->
                view?.showCurrentPodcast(entry)
            }
        }

    @FlowPreview
    override fun doOnAttach(view: PlayerContract.View) {
        observeCurrent()
        startUpdateProgress()
    }

    override fun pause() {
        entryRepository.pause()
    }

    override fun resume() {
        entryRepository.resume()
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

    override fun seekTo(timeLabel: TimeLabel) {
        timeLabel.time?.let{time ->
            view?.seekTo(time/1000)
        }
    }

    @FlowPreview
    private fun observeCurrent() {
        launch {
            entryRepository
                    .getCurrent()
                    .onEach { entry -> currentPodcast = entry }
                    .flatMapConcat{ entry -> entryRepository.getTimeLabels(entry) }
                    .collect{labels -> view?.showTimeLabels(labels)}
        }
    }

    private fun startUpdateProgress() {
        launch {
            startTimer(delayMillis = 0L, repeatMillis = 1000L) { view?.requestProgress() }
        }
    }
}