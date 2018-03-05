package su.tagir.apps.radiot.model.repository

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import su.tagir.apps.radiot.model.api.FirebaseRestClient
import su.tagir.apps.radiot.model.db.HostDao
import su.tagir.apps.radiot.model.entries.Host
import java.io.IOException

@RunWith(JUnit4::class)
class HostRepositoryTest {

    @Mock
    lateinit var hostDao: HostDao

    @Mock
    lateinit var firebaseRestClient: FirebaseRestClient

    private lateinit var hostRepository: HostRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        hostRepository = HostRepository(hostDao, firebaseRestClient)
    }

    @Test
    fun refreshHostsTest_success() {
        val hosts = listOf(Host(nickname = "qqq"), Host("www"))
        whenever(firebaseRestClient.getHosts()).thenReturn(Single.just(hosts))
        val observer = hostRepository.refreshHosts().test()
        observer.awaitTerminalEvent()

        verify(firebaseRestClient).getHosts()
        verify(hostDao).insertHosts(hosts)
        observer.assertComplete()
    }

    @Test
    fun refreshHostsTest_error() {
        val error = IOException()
        whenever(firebaseRestClient.getHosts()).thenReturn(Single.error(error))
        val observer = hostRepository.refreshHosts().test()
        observer.awaitTerminalEvent()

        verify(firebaseRestClient).getHosts()
        verify(hostDao, never()).insertHosts(any())
        observer.assertError(error)
    }

    @Test
    fun getHostsTest() {
        hostRepository.getHosts()
        verify(hostDao).findHosts()
    }
}