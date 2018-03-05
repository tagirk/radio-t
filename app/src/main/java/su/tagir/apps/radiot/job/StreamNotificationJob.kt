package su.tagir.apps.radiot.job

import android.preference.PreferenceManager
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import su.tagir.apps.radiot.ui.notification.createStreamNotification
import su.tagir.apps.radiot.ui.notification.notify
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import java.util.*
import java.util.concurrent.TimeUnit


class StreamNotificationJob : DailyJob() {

    override fun onRunDailyJob(params: Params): DailyJobResult {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val showNotification = prefs.getBoolean(SettingsFragment.KEY_NOTIF_STREAM, false)
        val moscowTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

        if (showNotification && moscowTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            val notif = createStreamNotification(context)
            notify(notif, 33, context)
        }

        return DailyJobResult.SUCCESS
    }

    companion object {

        const val TAG = "StreamNotificationJob"

        fun schedule(): Int {
           val requests =  JobManager.instance().getAllJobRequestsForTag(TAG)
            if(requests.isNotEmpty()){
                return requests.first().jobId
            }
            val moscowTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))
            moscowTime.set(Calendar.HOUR_OF_DAY, 22)
            moscowTime.set(Calendar.MINUTE, 0)
            moscowTime.set(Calendar.SECOND, 0)

            val local = Calendar.getInstance()
            local.timeInMillis = moscowTime.timeInMillis

            val end = local.get(Calendar.HOUR_OF_DAY).toLong()

            local.timeInMillis = local.timeInMillis - TimeUnit.HOURS.toMillis(2)

            val start = local.get(Calendar.HOUR_OF_DAY).toLong()

            return DailyJob.schedule(JobRequest.Builder(TAG), TimeUnit.HOURS.toMillis(start), TimeUnit.HOURS.toMillis(end))
        }
    }
}