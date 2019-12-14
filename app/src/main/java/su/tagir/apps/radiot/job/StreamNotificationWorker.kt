package su.tagir.apps.radiot.job

import android.content.Context
import androidx.work.*
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.ui.notification.createStreamNotification
import su.tagir.apps.radiot.ui.notification.notify
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration


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
        private const val SELF_REMINDER_HOUR = 23

        fun schedule(context: Context) {

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            calendar.timeInMillis = 0
            if(hours < SELF_REMINDER_HOUR){
                calendar.add(Calendar.HOUR, SELF_REMINDER_HOUR)
            }else{
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.HOUR, SELF_REMINDER_HOUR)
            }

            val request = PeriodicWorkRequest.Builder(StreamNotificationWorker::class.java,
                    24L,
                    TimeUnit.HOURS,
                    PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
                    TimeUnit.MILLISECONDS)
                    .setInitialDelay(calendar.timeInMillis, TimeUnit.MILLISECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }
}