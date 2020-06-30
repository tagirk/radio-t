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
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import su.tagir.apps.radiot.ui.mvp.MvpBasePresenter
import timber.log.Timber

class PlayerPresenter(private val entryRepository: EntryRepository,
                      private val router: Router,
                      dispatcher: CoroutineDispatcher = MainDispatcher()) : MvpBasePresenter<PlayerContract.View>(dispatcher), PlayerContract.Presenter {

    private var currentPodcast: Entry? = null
        set(value) {
            field = value
            value?.let { entry ->
                ifViewAttached({v -> v.showCurrentPodcast(entry)})
            }
        }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun doOnAttach(view: PlayerContract.View) {
        update()
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
        Timber.d("showChat")
        currentPodcast?.let { entry ->

            if (entry.url != STREAM_URL) {
                Timber.d("showChat: ${entry.chatUrl}")
                router.navigateTo(Screens.WebScreen(entry.chatUrl))
            } else {
//                router.navigateTo(Screens.ChatScreen)
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
            ifViewAttached({v -> v.seekTo(time.toInt() / 1000)})
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun update()  {
       launch {
            entryRepository
                    .getCurrent()
                    .onEach { entry -> currentPodcast = entry }
                    .flatMapLatest { entry -> entryRepository.getTimeLabels(entry) }
                    .collect { labels ->
                        ifViewAttached({v -> v.showTimeLabels(labels) })
                    }
        }
    }

    private fun startUpdateProgress() {
        launch {
            flow {
                while (true) {
                    emit(1)
                    delay(1000L)
                }
            }.collect { ifViewAttached({v -> v.requestProgress() })
            }
        }
    }
}