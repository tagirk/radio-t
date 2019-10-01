package su.tagir.apps.radiot.utils

import kotlinx.coroutines.delay

suspend fun startTimer(delayMillis: Long = 0, repeatMillis: Long = 0, action: () -> Unit) {
    delay(delayMillis)
    if (repeatMillis > 0) {
        while (true) {
            action()
            delay(repeatMillis)
        }
    } else {
        action()
    }
}