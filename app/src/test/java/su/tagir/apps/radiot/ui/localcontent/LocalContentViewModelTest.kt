package su.tagir.apps.radiot.ui.localcontent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockito_kotlin.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.util.createEntry

@RunWith(JUnit4::class)
class LocalContentViewModelTest {

    @Rule
    @JvmField
    val instantExecutor: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var entryRepository: EntryRepository

    private lateinit var viewModel: LocalContentViewModel

    private lateinit var router: Router

    @Before
    fun setup() {
        entryRepository = mock()

        router = mock()

        viewModel = LocalContentViewModel(entryRepository, router)
    }

    @Test
    fun testNotNull() {
        assertThat(viewModel.getEntry(), `is`(notNullValue()))
        verify(entryRepository, never()).getEntry(any())
        viewModel.setId("qq")
        verify(entryRepository, never()).getEntry(any())
    }

    @Test
    fun testCallRepo() {
        viewModel.getEntry().observeForever(mock())
        viewModel.setId("qq")
        verify(entryRepository).getEntry("qq")

        reset(entryRepository)
        viewModel.setId("ww")
        verify(entryRepository).getEntry("ww")
    }

    @Test
    fun sendResultToUi() {
        val foo = createEntry(1)
        val bar = createEntry(2)
        val fooData = MutableLiveData<Entry>()
        val barData = MutableLiveData<Entry>()
        whenever(entryRepository.getEntry("qq")).thenReturn(fooData)
        whenever(entryRepository.getEntry("ww")).thenReturn(barData)
        val observer: Observer<Entry?> = mock()

        viewModel.getEntry().observeForever(observer)
        viewModel.setId("qq")
        verify(observer, never()).onChanged(any())
        fooData.value = foo
        verify(observer).onChanged(foo)

        reset(observer)
        barData.value = bar
        viewModel.setId("ww")
        verify(observer).onChanged(bar)
    }

    @Test
    fun openInBrowserTest() {
        val entry = createEntry()
        val data = MutableLiveData<Entry>()
        data.value = entry
        whenever(entryRepository.getEntry("qq")).thenReturn(data)
        viewModel.getEntry().observeForever(mock())
        viewModel.setId("qq")
        viewModel.openInBrowser()
        verify(router).navigateTo(Screens.WEB_SCREEN, entry.url)
    }

    @Test
    fun onBackPressedTest() {
        viewModel.onBackPressed()
        verify(router).exit()
    }

}