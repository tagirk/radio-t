package su.tagir.apps.radiot.ui.news

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
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
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.schedulers.ImmediateSchedulerProvider
import su.tagir.apps.radiot.util.createEntry
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class NewsViewModelTest {


    @Rule
    @JvmField
    val instantExecutor: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var entryRepository: EntryRepository

    private lateinit var newsViewModel: NewsViewModel

    private lateinit var testScheduler: TestScheduler

    private lateinit var router: Router

    private val error = Throwable("Error!")

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        entryRepository = mock()
        router = mock()

        newsViewModel = NewsViewModel(entryRepository, ImmediateSchedulerProvider(), router)

    }

    @Test
    fun getDataTest() {
        whenever(entryRepository.getNews()).thenReturn(mock())
        newsViewModel.getData()
        verify(entryRepository).getNews()
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun loadDataTest() {
        whenever(entryRepository.refreshNews()).thenReturn(Completable.complete().delay(1, TimeUnit.SECONDS, testScheduler))

        assertThat(newsViewModel.firstLaunch, `is`(true))

        newsViewModel.loadData()

        assertThat(newsViewModel.state.value?.loading, `is`(true))
        verify(entryRepository).refreshNews()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(newsViewModel.state.value?.isCompleted(), `is`(true))
        assertThat(newsViewModel.firstLaunch, `is`(false))
    }

    @Test
    fun loadDataTest_error() {
        whenever(entryRepository.refreshNews()).thenReturn(Completable.error(error).delay(1, TimeUnit.SECONDS, testScheduler))
        assertThat(newsViewModel.firstLaunch, `is`(true))

        newsViewModel.loadData()

        assertThat(newsViewModel.state.value?.loading, `is`(true))
        verify(entryRepository).refreshNews()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(newsViewModel.state.value?.error, `is`(true))
        assertThat(newsViewModel.error.value == null, `is`(true))
        assertThat(newsViewModel.firstLaunch, `is`(false))
    }

    @Test
    fun refreshTest() {
        whenever(entryRepository.refreshNews()).thenReturn(Completable.complete().delay(1, TimeUnit.SECONDS, testScheduler))
        newsViewModel.update(true)
        assertThat(newsViewModel.state.value?.refreshing, `is`(true))
        verify(entryRepository).refreshNews()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        assertThat(newsViewModel.state.value?.isCompleted(), `is`(true))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun refreshTest_error() {
        whenever(entryRepository.refreshNews()).thenReturn(Completable.error(error).delay(1, TimeUnit.SECONDS, testScheduler))
        newsViewModel.update(true)
        assertThat(newsViewModel.state.value?.refreshing, `is`(true))
        verify(entryRepository).refreshNews()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        assertThat(newsViewModel.state.value?.error, `is`(true))
        assertThat(newsViewModel.error.value, `is`("Error!"))
        verifyNoMoreInteractions(entryRepository)
    }

    @Test
    fun onEntryClick_test() {
        val entry = createEntry()
        newsViewModel.onEntryClick(entry)
        verify(router).navigateTo(Screens.CONTENT_SCREEN, entry.url)
    }
}