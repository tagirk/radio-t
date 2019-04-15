package su.tagir.apps.radiot.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Article
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.model.entries.TimeLabel
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.BaseSchedulerProvider
import su.tagir.apps.radiot.ui.common.SingleLiveEvent
import su.tagir.apps.radiot.ui.viewmodel.BaseViewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerViewModel
@Inject constructor(private val entryRepository: EntryRepository,
                    private val router: Router,
                    scheduler: BaseSchedulerProvider) : BaseViewModel(scheduler) {

    private val error = SingleLiveEvent<String>()
    private val seekEvent = SingleLiveEvent<Long>()
    private val expandEvent = SingleLiveEvent<Void>()
    private val requestProgressEvent = SingleLiveEvent<Void>()
    private val loading = MutableLiveData<Boolean>()
    private val currentPodcast = MutableLiveData<Entry>()
    private val progress = MutableLiveData<Progress>()
    private val timeLabels=MutableLiveData<List<TimeLabel>>()
    private val sliding = MutableLiveData<Float>()

    private var progressDisposable: Disposable? = null

    init {
        currentPodcast.value = null
        disposable+=entryRepository.getCurrent()
                .doOnNext{currentPodcast.postValue(it)}
                .flatMap { entryRepository.getTimeLabels(it) }
                .retry(3)
                .subscribe({timeLabels.postValue(it)},{Timber.e(it)})

    }

    fun onSliding(slideOffset: Float) {
        sliding.value = slideOffset
    }

    fun getCurrentPodcast() = currentPodcast

    fun getTimeLabels() = timeLabels

    fun onPlayClick(podcast: Entry) {
        entryRepository.play(podcast)
    }

    fun onPauseClick() {
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
            router.navigateTo(Screens.WebScreen(entry.chatUrl))
        } else {
            showStreamChat()
        }
    }

    fun showStreamChat() {
        router.navigateTo(Screens.ChatScreen)
    }

    fun openWebPage() {
        val entry = currentPodcast.value ?: return
        router.navigateTo(Screens.WebScreen(entry.url))
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
        article?.link?.let{
            router.navigateTo(Screens.WebScreen(article.link))
        }

    }

    fun setProgress(progress: Progress){
        this.progress.postValue(progress)
    }

    fun getProgress(): LiveData<Progress> = progress

    fun setLoading(loading: Boolean){
        this.loading.postValue(loading)
    }

    fun isLoading(): LiveData<Boolean> = loading

    fun getSlidingValue(): LiveData<Float> = sliding

    fun setError(error: String?){
        this.error.postValue(error)
    }

    fun getError(): LiveData<String> = error

    fun seekEvent(): LiveData<Long> = seekEvent

    fun requestProgressEvent(): LiveData<Void> = requestProgressEvent

    fun expandEvent(): LiveData<Void> = expandEvent
}