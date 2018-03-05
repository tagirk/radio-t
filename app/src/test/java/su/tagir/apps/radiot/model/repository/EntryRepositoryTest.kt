package su.tagir.apps.radiot.model.repository

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Single
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import su.tagir.apps.radiot.model.api.RestClient
import su.tagir.apps.radiot.model.db.EntryDao
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.SearchResult
import su.tagir.apps.radiot.model.repository.EntryRepository.Companion.PAGE_SIZE
import su.tagir.apps.radiot.util.createEntry
import su.tagir.apps.radiot.util.createRTEntries
import su.tagir.apps.radiot.util.successCall
import java.io.IOException


@RunWith(JUnit4::class)
class EntryRepositoryTest {

    @Rule
    @JvmField
    val instantExecutor: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var entryDao: EntryDao

    @Mock
    private lateinit var restClient: RestClient

    @Mock
    private lateinit var downloadManager: DownloadManager

    @Mock
    private lateinit var application: Application

    private lateinit var entryRepository: EntryRepository

    private val error = IOException()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        entryRepository = EntryRepository(restClient, entryDao, downloadManager, application)
    }

    @Test
    fun getCurrentTest() {
        whenever(entryDao.getCurrentEntryLive()).thenReturn(MutableLiveData())
        entryRepository.getCurrent()
        verify(entryDao).getCurrentEntryLive()
        verifyNoMoreInteractions(entryDao)
    }

    @Test
    fun getTimeLabelsTest() {
        val currentEntry = createEntry()
        whenever(entryDao.getTimeLabels(currentEntry.date)).thenReturn(MutableLiveData())
        entryRepository.getTimeLabels(currentEntry)
        verify(entryDao).getTimeLabels(currentEntry.date)
        verifyNoMoreInteractions(entryDao)
    }

    @Test
    fun getPodcasts() {
        entryRepository.getPodcasts()
        verify(entryDao).getPodcasts()
        verifyNoMoreInteractions(entryDao)
    }

    @Test
    fun refreshPodcastsTest_success() {
        val entries = createRTEntries(PAGE_SIZE)
        val call = successCall(entries)

        whenever(restClient.getPosts(PAGE_SIZE, "podcast")).thenReturn(call)
        val observer = entryRepository.refreshPodcasts().test()
        observer.awaitTerminalEvent()
        verify(restClient).getPosts(PAGE_SIZE, "podcast")
        verify(entryDao).saveRadioTEntries(entries)
        observer
                .assertComplete()
    }

    @Test
    fun refreshPodcastsTest_error() {
        whenever(restClient.getPosts(PAGE_SIZE, "podcast")).thenReturn(Single.error(error))
        val observer = entryRepository.refreshPodcasts().test()
        observer.awaitTerminalEvent()
        verify(restClient).getPosts(PAGE_SIZE, "podcast")
        observer
                .assertError(error)
    }

    @Test
    fun refreshNewsTest_success() {
        val entries = createRTEntries(PAGE_SIZE)
        val call = successCall(entries)

        whenever(restClient.getPosts(PAGE_SIZE, "news,info")).thenReturn(call)
        val observer = entryRepository.refreshNews().test()
        observer.awaitTerminalEvent()
        verify(restClient).getPosts(PAGE_SIZE, "news,info")
        verify(entryDao).saveRadioTEntries(entries)
        observer
                .assertComplete()
    }

    @Test
    fun refreshNewsTest_error() {
        whenever(restClient.getPosts(PAGE_SIZE, "news,info")).thenReturn(Single.error(error))
        val observer = entryRepository.refreshNews().test()
        observer.awaitTerminalEvent()
        verify(restClient).getPosts(PAGE_SIZE, "news,info")
        observer
                .assertError(error)
    }

    @Test
    fun searchTest_success() {
        val entries = createRTEntries(PAGE_SIZE)

        whenever(restClient.search("docker", 0, PAGE_SIZE)).thenReturn(Single.just(entries))
        val savedSearchResult = ArgumentCaptor.forClass(SearchResult::class.java)

        val observer = entryRepository.search("docker").test()
        observer.awaitTerminalEvent()

        verify(restClient).search("docker", 0, PAGE_SIZE)

        verify(entryDao).saveSearchResult(savedSearchResult.capture(), eq(entries))

        assertThat(savedSearchResult.value.query, `is`("docker"))
        assertThat(savedSearchResult.value.ids.size , `is`(PAGE_SIZE))

        observer
                .assertComplete()
    }

    @Test
    fun searchTest_error() {
        whenever(restClient.search("docker", 0, PAGE_SIZE)).thenReturn(Single.error(error))
        val observer = entryRepository.search("docker").test()
        observer.awaitTerminalEvent()

        verify(restClient).search("docker", 0, PAGE_SIZE)

        observer
                .assertError(error)
    }

    @Test
    fun searchNextPage_success() {
        val searchResult = SearchResult("docker", listOf("1", "2", "3"))
        val entries = createRTEntries(PAGE_SIZE)

        whenever(entryDao.findSearchResult("docker")).thenReturn(searchResult)
        whenever(restClient.search("docker", 3, PAGE_SIZE)).thenReturn(Single.just(entries))

        val observer = entryRepository.searchNextPage("docker").test()
        observer.awaitTerminalEvent()

        verify(entryDao).findSearchResult("docker")
        verify(restClient).search("docker", searchResult.ids.size, PAGE_SIZE)

        val captor = ArgumentCaptor.forClass(SearchResult::class.java)
        verify(entryDao).saveSearchResult(captor.capture(), eq(entries))

        assertThat(captor.value.query, `is`("docker"))
        assertThat(captor.value.ids.size, `is`(PAGE_SIZE + searchResult.ids.size))

        observer
                .assertComplete()
    }

    @Test
    fun searchNextPage_error() {
        val searchResult = SearchResult("docker", listOf("1", "2", "3"))

        whenever(entryDao.findSearchResult("docker")).thenReturn(searchResult)
        whenever(restClient.search("docker", 3, PAGE_SIZE)).thenReturn(Single.error(error))

        val observer = entryRepository.searchNextPage("docker").test()
        observer.awaitTerminalEvent()

        verify(entryDao).findSearchResult("docker")
        verify(restClient).search("docker", searchResult.ids.size, PAGE_SIZE)

        observer
                .assertError(error)
    }

    @Test
    fun getForQueryTest() {
        val searchResult = SearchResult("docker", listOf("1", "2", "3"))
        val liveData = MutableLiveData<SearchResult>()
        liveData.value = searchResult
        whenever(entryDao.findSearchResultLive("docker")).thenReturn(liveData)
        entryRepository.getForQuery("docker").observeForever(mock())

        verify(entryDao).findSearchResultLive("docker")
        verify(entryDao).loadById(searchResult.ids)
        verifyNoMoreInteractions(entryDao)
    }

    @Test
    fun removeQueryTest() {
        entryRepository.removeQuery("docker")
        verify(entryDao).removeQuery("docker")
        verifyNoMoreInteractions(entryDao)
    }

    @Test
    fun startDownloadTest() {
        whenever(downloadManager.startDownload("qqqq")).thenReturn(123L)
        val captor = ArgumentCaptor.forClass(Long::class.java)
        val observer = entryRepository.startDownload("qqqq").test()
        verify(entryDao).updateDownloadId(captor.capture(), eq("qqqq"))
        assertThat(captor.value, `is`(123L))
        observer
                .assertComplete()
    }

    @Test
    fun checkDownloadStatusTest_empty() {
        whenever(entryDao.getDownloadIds()).thenReturn(emptyList())
        entryRepository.checkDownloadStatus()
        verify(downloadManager, never()).checkDownloadStatus(any(), any(), any())
        verify(entryDao, never()).updateDownloadStatus(any(), any(), any())
    }

    @Test
    fun checkDownloadStatusTest() {
        val ids = mutableListOf(1L, 2L, 3L)

        whenever(entryDao.getDownloadIds()).thenReturn(ids)

        entryRepository.checkDownloadStatus()

        verify(downloadManager).checkDownloadStatus(eq(ids), any(), any())
        verify(entryDao).updateDownloadStatus(eq(ids), any(), any())
    }

    @Test
    fun deleteFileTest() {
        whenever(downloadManager.delete(1L)).thenReturn(1)

        val observer = entryRepository.deleteFile(1L).test()
        val captor = ArgumentCaptor.forClass(Long::class.java)
        verify(entryDao).deleteFilePath(captor.capture())
        assertThat(captor.value, `is`(1L))

        observer
                .assertComplete()

    }

    @Test
    fun deleteFileTest_error() {
        whenever(downloadManager.delete(1L)).thenReturn(0)
        val observer = entryRepository.deleteFile(1L).test()
        verify(downloadManager).delete(1L)
        verify(entryDao, never()).deleteFilePath(any())

        observer
                .assertError(Throwable::class.java)
    }

    @Test
    fun playTest() {
        val entry = createEntry()
        entryRepository.play(entry)
        verify(application).startService(any())
    }

    @Test
    fun pauseTest() {
        entryRepository.pause()
        verify(application).startService(any())
    }

    @Test
    fun resumeTest() {
        entryRepository.pause()
        verify(application).startService(any())
    }

    @Test
    fun playStreamTest() {
        entryRepository.playStream("qqq")

        verify(entryDao).playStream("qqq")
        verify(application).startService(any())
    }


    @Test
    fun setCurrentEntryTest(){
        val urlCaptor = ArgumentCaptor.forClass(String::class.java)
        val progressCaptor = ArgumentCaptor.forClass(Long::class.java)
        entryRepository.setCurrentEntry("qqq", 76)
        verify(entryDao).setCurrentEntry(urlCaptor.capture(), progressCaptor.capture())
        assertThat(urlCaptor.value, `is`("qqq"))
        assertThat(progressCaptor.value, `is`(76L))
        verifyNoMoreInteractions(entryDao)
    }

    @Test
    fun updateCurrentEntryStateAndProgressTest(){
        val stateCaptor = ArgumentCaptor.forClass(Int::class.java)
        val progressCaptor = ArgumentCaptor.forClass(Long::class.java)
        entryRepository.updateCurrentEntryStateAndProgress(EntryState.PLAYING, 76L)
        verify(entryDao).updateCurrentEntryStateAndProgress(stateCaptor.capture(), progressCaptor.capture())
        assertThat(stateCaptor.value, `is`(EntryState.PLAYING))
        assertThat(progressCaptor.value, `is`(76L))
        verifyNoMoreInteractions(entryDao)
    }

}