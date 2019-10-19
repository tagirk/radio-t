package su.tagir.apps.radiot.job

import android.content.Context
import androidx.work.*
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.ui.notification.createStreamNotification
import su.tagir.apps.radiot.ui.notification.notify
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import java.util.*
import java.util.concurrent.TimeUnit


class StreamNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = (applicationContext as App).appComponent.preferences

        val showNotification = prefs.getBoolean(SettingsFragment.KEY_NOTIF_STREAM, false)

        if(!showNotification){
            return Result.success()
        }
        val moscowTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

        if (moscowTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            val notif = createStreamNotification(applicationContext)
            notify(notif, 33, applicationContext)
        }
        return Result.success()
    }


    companion object {

        const val TAG = "StreamNotificationWorker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequest.Builder(StreamNotificationWorker::class.java, 1L, TimeUnit.DAYS, 4L, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}