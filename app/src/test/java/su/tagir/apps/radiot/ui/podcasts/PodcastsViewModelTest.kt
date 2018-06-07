package su.tagir.apps.radiot.ui.podcasts

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.ImmediateSchedulerProvider
import su.tagir.apps.radiot.util.createEntry
import java.util.concurrent.TimeUnit


@RunWith(JUnit4::class)
class PodcastsViewModelTest {

    private val entry = createEntry()

    @Rule
    @JvmField
    val instantExecutor: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var entryRepository: EntryRepository

    private lateinit var podcastsViewModel: PodcastsViewModel

    private lateinit var testScheduler: TestScheduler

    private val error = Throwable("Error!")

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        entryRepository = mock()

        podcastsViewModel = PodcastsViewModel(entryRepository, ImmediateSchedulerProvider())

    }

    @Test
    fun getDataTest() {
        whenever(entryRepository.getPodcasts()).thenReturn(mock())
        podcastsViewModel.getData()
        verify(entryRepository).getPodcasts()
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun loadDataTest() {
        whenever(entryRepository.refreshPodcasts()).thenReturn(Completable.complete().delay(1, TimeUnit.SECONDS, testScheduler))

        assertThat(podcastsViewModel.firstLaunch, `is`(true))

        podcastsViewModel.loadData()

        assertThat(podcastsViewModel.state.value?.loading, `is`(true))
        verify(entryRepository).refreshPodcasts()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(podcastsViewModel.state.value?.isCompleted(), `is`(true))
        assertThat(podcastsViewModel.firstLaunch, `is`(false))
    }

    @Test
    fun loadDataTest_error() {
        whenever(entryRepository.refreshPodcasts()).thenReturn(Completable.error(error).delay(1, TimeUnit.SECONDS, testScheduler))
        assertThat(podcastsViewModel.firstLaunch, `is`(true))

        podcastsViewModel.loadData()

        assertThat(podcastsViewModel.state.value?.loading, `is`(true))
        verify(entryRepository).refreshPodcasts()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(podcastsViewModel.state.value?.error, `is`(true))
        assertThat(podcastsViewModel.downloadError.value == null, `is`(true))
        assertThat(podcastsViewModel.firstLaunch, `is`(false))
    }

    @Test
    fun refreshTest() {
        whenever(entryRepository.refreshPodcasts()).thenReturn(Completable.complete().delay(1, TimeUnit.SECONDS, testScheduler))
        podcastsViewModel.update(true)
        assertThat(podcastsViewModel.state.value?.refreshing, `is`(true))
        verify(entryRepository).refreshPodcasts()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        assertThat(podcastsViewModel.state.value?.isCompleted(), `is`(true))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun refreshTest_error() {
        whenever(entryRepository.refreshPodcasts()).thenReturn(Completable.error(error).delay(1, TimeUnit.SECONDS, testScheduler))
        podcastsViewModel.update(true)
        assertThat(podcastsViewModel.state.value?.refreshing, `is`(true))
        verify(entryRepository).refreshPodcasts()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        assertThat(podcastsViewModel.state.value?.error, `is`(true))
        assertThat(podcastsViewModel.downloadError.value, `is`("Error!"))
        verifyNoMoreInteractions(entryRepository)
    }


    @Test
    fun onDownloadClickTest() {
        whenever(entryRepository.startDownload(entry.audioUrl)).thenReturn(Completable.complete())
        podcastsViewModel.onDownloadClick(entry)
        verify(entryRepository).startDownload(entry.audioUrl)
        assertThat(podcastsViewModel.downloadError.value == null, `is`(true))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun onDownloadClickTest_error() {
        whenever(entryRepository.startDownload(entry.audioUrl)).thenReturn(Completable.error(error))
        podcastsViewModel.onDownloadClick(entry)
        verify(entryRepository).startDownload(entry.audioUrl)
        assertThat(podcastsViewModel.downloadError.value, `is`("Error!"))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun onRemoveClickTest() {
        whenever(entryRepository.deleteFile(1L)).thenReturn(Completable.complete())
        podcastsViewModel.onRemoveClick(entry)
        verify(entryRepository).deleteFile(entry.downloadId)
        assertThat(podcastsViewModel.downloadError.value == null, `is`(true))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun onRemoveClickTest_error() {
        whenever(entryRepository.deleteFile(1L)).thenReturn(Completable.error(error))
        podcastsViewModel.onRemoveClick(entry)
        verify(entryRepository).deleteFile(entry.downloadId)
        assertThat(podcastsViewModel.downloadError.value, `is`("Error!"))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun statusTimer_test() {
        podcastsViewModel.startStatusTimer()
        assertThat(podcastsViewModel.intervalDisposable?.isDisposed, `is`(false))

        podcastsViewModel.stopStatusTimer()
        assertThat(podcastsViewModel.intervalDisposable?.isDisposed, `is`(true))
    }


}