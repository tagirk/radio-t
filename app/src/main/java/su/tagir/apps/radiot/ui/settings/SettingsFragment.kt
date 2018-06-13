package su.tagir.apps.radiot.ui.settings

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.app.AppCompatDelegate.*
import android.support.v7.preference.PreferenceFragmentCompat
import com.evernote.android.job.JobManager
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.job.StreamNotificationJob
import su.tagir.apps.radiot.ui.MainViewModel
import javax.inject.Inject


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel


    companion object {
        const val KEY_NOTIF_STREAM = "stream_notification"
        const val KEY_CRASH_REPORTS = "crash_reports"
        const val KEY_NIGHT_MODE = "night_mode"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefs = preferenceScreen.sharedPreferences

        val streamNotif = findPreference(KEY_NOTIF_STREAM)
        streamNotif.summary = if (prefs.getBoolean(KEY_NOTIF_STREAM, false)) getString(R.string.show) else getString(R.string.not_show)

        val crashNotif = findPreference(KEY_CRASH_REPORTS)
        crashNotif.summary = if (prefs.getBoolean(KEY_CRASH_REPORTS, false)) getString(R.string.send) else getString(R.string.not_send)

        val modes = resources.getStringArray(R.array.night_mode)

        val nightMode = findPreference(KEY_NIGHT_MODE)
        nightMode.summary = prefs.getString(KEY_NIGHT_MODE, modes[0])


    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setCurrentScreen(Screens.SETTINGS_SCREEN)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        when (key) {
            KEY_NOTIF_STREAM -> {
                val show = prefs.getBoolean(key, false)
                val streamNotif = findPreference(key)
                streamNotif.summary = if (show) getString(R.string.show) else getString(R.string.not_show)
                scheduleOrRemoveNotifJob(show)

            }
            KEY_CRASH_REPORTS -> {
                val crashNotif = findPreference(key)
                crashNotif.summary = if (prefs.getBoolean(key, false)) getString(R.string.send) else getString(R.string.not_send)
            }
            KEY_NIGHT_MODE -> {
                val modes = resources.getStringArray(R.array.night_mode)
                val mode = prefs.getString(key, modes[0])
                val nightMode = findPreference(key)
                nightMode.summary = mode
                when (mode) {
                    modes[2] -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO)
                    modes[1] -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }
                activity?.recreate()
            }
        }
    }

    private fun scheduleOrRemoveNotifJob(notify: Boolean) {
        if (notify) {
            StreamNotificationJob.schedule()
        } else {
            JobManager.instance()
                    .getAllJobRequestsForTag(StreamNotificationJob.TAG)
                    .forEach { it.cancelAndEdit() }
        }
    }
}