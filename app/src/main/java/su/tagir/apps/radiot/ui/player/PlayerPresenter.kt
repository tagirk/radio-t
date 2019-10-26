package su.tagir.apps.radiot.ui.player

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import timber.log.Timber

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

    @ExperimentalCoroutinesApi
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
        timeLabel.time?.let { time ->
            view?.seekTo(time / 1000)
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun observeCurrent() {
        launch {
            entryRepository
                    .getCurrent()
                    .onEach { entry -> currentPodcast = entry }
                    .flatMapLatest { entry -> entryRepository.getTimeLabels(entry) }
                    .collect { labels ->
                        Timber.d("$labels")
                        view?.showTimeLabels(labels) }
        }
    }

    private fun startUpdateProgress() {
        launch {
            flow {
                while (true) {
                    emit(1)
                    delay(1000L)
                }
            }.collect { view?.requestProgress() }
        }
    }
}