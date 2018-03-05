package su.tagir.apps.radiot.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import com.evernote.android.job.JobManager
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.job.StreamNotificationJob


class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val KEY_NOTIF_STREAM = "stream_notification"
        const val KEY_CRASH_REPORTS = "crash_reports"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        val prefs = preferenceScreen.sharedPreferences

        val streamNotif = findPreference(KEY_NOTIF_STREAM)
        streamNotif.summary = if(prefs.getBoolean(KEY_NOTIF_STREAM, false)) getString(R.string.show) else getString(R.string.not_show)

        val crashNotif = findPreference(KEY_CRASH_REPORTS)
        crashNotif.summary = if(prefs.getBoolean(KEY_CRASH_REPORTS, false)) getString(R.string.send) else getString(R.string.not_send)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        when(key){
            KEY_NOTIF_STREAM->{
                val show = prefs.getBoolean(key, false)
                val streamNotif = findPreference(key)
                streamNotif.summary = if(show) getString(R.string.show) else getString(R.string.not_show)
                scheduleOrRemoveNotifJob(show)

            }
            KEY_CRASH_REPORTS->{
                val crashNotif = findPreference(key)
                crashNotif.summary = if(prefs.getBoolean(key, false)) getString(R.string.send) else getString(R.string.not_send)
            }
        }
    }

    private fun scheduleOrRemoveNotifJob(notify: Boolean){
        if(notify){
            StreamNotificationJob.schedule()
        }else{
            JobManager.instance()
                    .getAllJobRequestsForTag(StreamNotificationJob.TAG)
                    .forEach { it.cancelAndEdit() }
        }
    }
}