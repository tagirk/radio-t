package su.tagir.apps.radiot.ui.hosts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import su.tagir.apps.radiot.model.repository.HostRepository
import su.tagir.apps.radiot.schedulers.ImmediateSchedulerProvider
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class HostsViewModelTest {

    @Rule
    @JvmField
    val instantExecutor: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var hostsRepository: HostRepository

    private lateinit var router: Router

    private lateinit var hostsViewModel: HostsViewModel

    private lateinit var testScheduler: TestScheduler

    private val error = Throwable("Error!")

    @Before
    fun setup() {
        testScheduler = TestScheduler()
        hostsRepository = mock()
        router = mock()

        hostsViewModel = HostsViewModel(hostsRepository, router, ImmediateSchedulerProvider())
    }

    @Test
    fun getDataTest() {
        whenever(hostsRepository.getHosts()).thenReturn(mock())
        hostsViewModel.getData()
        verify(hostsRepository).getHosts()
        verifyNoMoreInteractions(hostsRepository)
    }


    @Test
    fun loadDataTest() {
        whenever(hostsRepository.refreshHosts()).thenReturn(Completable.complete().delay(1, TimeUnit.SECONDS, testScheduler))

        assertThat(hostsViewModel.firstLaunch, `is`(true))

        hostsViewModel.loadData()

        assertThat(hostsViewModel.state.value?.loading, `is`(true))
        verify(hostsRepository).refreshHosts()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(hostsViewModel.state.value?.isCompleted(), `is`(true))
        assertThat(hostsViewModel.firstLaunch, `is`(false))
    }

    @Test
    fun loadDataTest_error() {
        whenever(hostsRepository.refreshHosts()).thenReturn(Completable.error(error).delay(1, TimeUnit.SECONDS, testScheduler))

        assertThat(hostsViewModel.firstLaunch, `is`(true))

        hostsViewModel.loadData()

        assertThat(hostsViewModel.state.value?.loading, `is`(true))
        verify(hostsRepository).refreshHosts()

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        assertThat(hostsViewModel.state.value?.error, `is`(true))
        assertThat(hostsViewModel.error.value == null, `is`(true))
        assertThat(hostsViewModel.firstLaunch, `is`(false))
    }

    @Test
    fun refreshTest() {
        whenever(hostsRepository.refreshHosts()).thenReturn(Completable.complete().delay(1, TimeUnit.SECONDS, testScheduler))
        hostsViewModel.update(true)
        assertThat(hostsViewModel.state.value?.refreshing, `is`(true))
        verify(hostsRepository).refreshHosts()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        assertThat(hostsViewModel.state.value?.isCompleted(), `is`(true))
        verifyNoMoreInteractions(hostsRepository)
    }

    @Test
    fun refreshTest_error() {
        whenever(hostsRepository.refreshHosts()).thenReturn(Completable.error(error).delay(1, TimeUnit.SECONDS, testScheduler))
        hostsViewModel.update(true)
        assertThat(hostsViewModel.state.value?.refreshing, `is`(true))
        verify(hostsRepository).refreshHosts()
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)
        assertThat(hostsViewModel.state.value?.error, `is`(true))
        assertThat(hostsViewModel.error.value, `is`("Error!"))
        verifyNoMoreInteractions(hostsRepository)
    }

    @Test
    fun openSocialNetTest() {
        hostsViewModel.openSocialNet("qqqq")
        verify(router).navigateTo(Screens.RESOLVE_ACTIVITY, "qqqq")
    }

}