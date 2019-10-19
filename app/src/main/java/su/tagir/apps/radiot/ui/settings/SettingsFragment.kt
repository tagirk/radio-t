package su.tagir.apps.radiot.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.WorkManager
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.extensions.modify
import su.tagir.apps.radiot.job.StreamNotificationWorker

class SettingsFragmentRoot: Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val appComponent: AppComponent = (activity!!.application as App).appComponent

        val v = inflater.inflate(R.layout.fragment_toolbar, container, false)
        val toolbar = v.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener { appComponent.router.exit() }
        toolbar.setTitle(R.string.settings)

        if (childFragmentManager.findFragmentById(R.id.container) == null){
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, SettingsFragment())
                    .commitAllowingStateLoss()
        }

        return v
    }
}

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val KEY_NOTIF_STREAM = "stream_notification"
        const val KEY_CRASH_REPORTS = "crash_reports"
        const val KEY_NIGHT_MODE = "night_mode"
    }


    private lateinit var prefs: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val appComponent: AppComponent = (activity!!.application as App).appComponent
        prefs = appComponent.preferences
    }

    override fun onResume() {
        super.onResume()

        val streamNotif: SwitchPreference? = findPreference(KEY_NOTIF_STREAM)
        streamNotif?.summary = if (prefs.getBoolean(KEY_NOTIF_STREAM, false)) getString(R.string.show) else getString(R.string.not_show)
        streamNotif?.setOnPreferenceChangeListener { _, newValue ->
            prefs.modify { putBoolean(KEY_NOTIF_STREAM, newValue as Boolean) }
            true
        }

        val crashNotif: SwitchPreference? = findPreference(KEY_CRASH_REPORTS)
        crashNotif?.summary = if (prefs.getBoolean(KEY_CRASH_REPORTS, false)) getString(R.string.send) else getString(R.string.not_send)
        crashNotif?.setOnPreferenceChangeListener { _, newValue ->
            prefs.modify { putBoolean(KEY_CRASH_REPORTS, newValue as Boolean) }
            true
        }

        val modes = resources.getStringArray(R.array.night_mode)

        val nightMode: ListPreference? = findPreference(KEY_NIGHT_MODE)
        nightMode?.summary = prefs.getString(KEY_NIGHT_MODE, modes[0])
        nightMode?.setOnPreferenceChangeListener { _, newValue ->
            prefs.modify { putString(KEY_NIGHT_MODE, newValue as String) }
            true
        }

        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        when (key) {
            KEY_NOTIF_STREAM -> {
                val show = prefs.getBoolean(key, false)
                val streamNotif: SwitchPreference? = findPreference(key)
                streamNotif?.summary = if (show) getString(R.string.show) else getString(R.string.not_show)
                scheduleOrRemoveNotifJob(show)

            }
            KEY_CRASH_REPORTS -> {
                val crashNotif: SwitchPreference? = findPreference(key)
                crashNotif?.summary = if (prefs.getBoolean(key, false)) getString(R.string.send) else getString(R.string.not_send)
            }
            KEY_NIGHT_MODE -> {
                val modes = resources.getStringArray(R.array.night_mode)
                val mode = prefs.getString(key, modes[0])
                val nightMode: ListPreference? = findPreference(key)
                nightMode?.summary = mode
                when (mode) {
                    modes[2] -> setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY)
                    modes[1] -> setDefaultNightMode(MODE_NIGHT_YES)
                    else -> setDefaultNightMode(MODE_NIGHT_NO)
                }
                activity?.recreate()
            }
        }
    }

    private fun scheduleOrRemoveNotifJob(notify: Boolean) {
        context?.let {c ->
            if (notify) {
                StreamNotificationWorker.schedule(c)
            } else {
                WorkManager.getInstance(c)
                        .cancelAllWorkByTag(StreamNotificationWorker.TAG)
            }
        }
    }
}