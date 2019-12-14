package su.tagir.apps.radiot.model.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import su.tagir.apps.radiot.util.getValue
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@RunWith(JUnit4::class)
class RestClientsTest {

    private lateinit var restClient: RestClient


    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup(){
        mockWebServer = MockWebServer()

        restClient = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(RestClient::class.java)
    }

    @After
    fun shutdown(){
        mockWebServer.shutdown()
    }

    @Test
    fun getEntriesTest(){
        val count = 20
        enqueueResponse("entries.json")
        val entries = getValue(restClient.getPosts(count))

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/last/$count"))
        assertThat(entries, notNullValue())
        assertThat(entries.size, `is`(count))
    }

    @Test
    fun searchTest(){
        val query = "google"
        val skip = 10
        val limit = 20
        enqueueResponse("search.json")
        val entries = getValue(restClient.search(query, skip, limit))

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/search?q=$query&skip=$skip&limit=$limit"))
        assertThat(entries, notNullValue())
        assertThat(entries.size, `is`(limit))
    }

    @Throws(IOException::class)
    private fun enqueueResponse(fileName: String) {
        enqueueResponse(fileName, Collections.emptyMap())
    }

    @Throws(IOException::class)
    private fun enqueueResponse(fileName: String, headers: Map<String, String>) {
        val inputStream = javaClass.classLoader!!.getResourceAsStream("api_response/$fileName")
        val source = Okio.buffer(Okio.source(inputStream))
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(mockResponse
                .setBody(source.readString(StandardCharsets.UTF_8)))
    }
}