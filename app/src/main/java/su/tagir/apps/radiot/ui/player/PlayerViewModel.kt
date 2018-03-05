package su.tagir.apps.radiot.ui.player

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.repository.ChatRepository
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.AbsentLiveData
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.BaseViewModel
import su.tagir.apps.radiot.utils.getDistinct
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    private val chatRepository: ChatRepository,
                    private val router: Router,
                    scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    internal val error = SingleLiveEvent<String>()
    internal val seekEvent = SingleLiveEvent<Long>()
    val expandEvent = SingleLiveEvent<Void>()
    internal val requestProgressEvent = SingleLiveEvent<Void>()
    internal val loading = MutableLiveData<Boolean>()
    private val currentPodcast = entryRepository.getCurrent()
    internal val progress = MutableLiveData<Progress>()
    private val timeLabels: LiveData<List<TimeLabel>>

    internal val sliding = MutableLiveData<Float>()

    private var progressDisposable: Disposable? = null

    init {
        timeLabels = Transformations
                .switchMap(currentPodcast,
                        { currentPodcast ->
                            if (currentPodcast == null) {
                                AbsentLiveData.create()
                            } else {
                                entryRepository.getTimeLabels(currentPodcast).getDistinct()
                            }
                        })

    }

    fun onSliding(slideOffset: Float) {
        sliding.value = slideOffset
    }

    internal fun getCurrentPodcast() = currentPodcast

    internal fun getTimeLabels() = timeLabels

    internal fun onPlayClick(podcast: Entry) {
        entryRepository.play(podcast)
    }

    internal fun onPauseClick() {
        entryRepository.pause()
    }

    internal fun onResumeClick() {
        entryRepository.resume()
    }

    fun onPlayStreamClick() {
        entryRepository.playStream(STREAM_URL)
    }

    fun onChatClick() {
        val entry = currentPodcast.value ?: return

        if (entry.url != STREAM_URL) {
            router.navigateTo(Screens.WEB_SCREEN, "https://chat.radio-t.com/logs/radio-t-${entry.title?.substring(8)}.html")
        } else {
            showStreamChat()
        }
    }

    fun showStreamChat() {
        router.navigateTo(Screens.CHAT_ACTIVITY)
    }

    fun openWebPage() {
        val entry = currentPodcast.value ?: return
        router.navigateTo(Screens.WEB_SCREEN, entry.url)
    }

    internal fun seekTo(secs: Long) {
        seekEvent.value = secs
    }

    fun startUpdateProgress() {
        if (progressDisposable?.isDisposed == false) {
            return
        }
        progressDisposable = Observable
                .interval(0L, 1L, TimeUnit.SECONDS)
                .observeOn(scheduler.ui())
                .subscribe({ requestProgressEvent.call() }, { Timber.d(it) })

        addDisposable(progressDisposable!!)
    }

    fun stopUpdateProgress(){
        progressDisposable?.dispose()
    }

    fun onExpandClick(){
        expandEvent.call()
    }

    fun onArticleClick(article: Article?){
        router.navigateTo(Screens.WEB_SCREEN, article?.link)
    }
}