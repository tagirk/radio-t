package su.tagir.apps.radiot.model.repository

import android.os.SystemClock
import java.util.concurrent.TimeUnit

class RateLimiter<in KEY>(timeout: Long, timeUnit: TimeUnit) {

    private val timestamps = HashMap<KEY, Long>()
    private val timeout: Long = timeUnit.toMillis(timeout)

    @Synchronized
    fun shouldFetch(key: KEY): Boolean {
        val lastFetched = timestamps[key]
        val now = now()
        if (lastFetched == null) {
            timestamps[key] = now
            return true
        }
        if (now - lastFetched > timeout) {
            timestamps[key] = now
            return true
        }
        return false
    }

    private fun now(): Long {
        return SystemClock.uptimeMillis()
    }

    @Synchronized
    fun reset(key: KEY) {
        timestamps.remove(key)
    }

}