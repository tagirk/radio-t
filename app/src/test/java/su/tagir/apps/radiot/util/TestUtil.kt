package su.tagir.apps.radiot.util


import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import retrofit2.Response
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.RTEntry
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <T> successCall(data: T): Single<T> {
    return createCall(Response.success(data))
}

fun <T> createCall(response: Response<T>): Single<T> {
    return Single.just(response.body())
}

fun <T> getValue(single: Single<T>): T {
    val data = Array<Any?>(1, {})
    val latch = CountDownLatch(1)
    val observer = object : SingleObserver<T> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
        }


        override fun onSuccess(value: T) {
            data[0] = value
            latch.countDown()
        }

    }
    single.subscribe(observer)
    latch.await(1, TimeUnit.SECONDS)

    @Suppress("UNCHECKED_CAST")
    return data[0] as T
}

fun createEntry(): Entry {
    return createEntry(1)
}

fun createEntry(num:Int): Entry {
    return Entry(url = "www.$num.tt",
            title = "title_$num",
            audioUrl = "www.audio_$num.tt",
            date = Date(1000L * num),
            downloadId = num.toLong())
}

fun createEntries(count:Int): List<Entry>{
    return count
            .downTo(1)
            .map { createEntry(it) }


}

fun createRTEntry(num:Int): RTEntry {
    return RTEntry(url = "www.$num.tt",
            title = "title_$num",
            audioUrl = "www.audio_$num.tt",
            date = Date(1000L * num))
}

fun createRTEntries(count:Int): List<RTEntry>{
    return count
            .downTo(1)
            .map { createRTEntry(it) }


}

