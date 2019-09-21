package su.tagir.apps.radiot.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable
import timber.log.Timber

class AboutFragment : PreferenceFragmentCompat(), Injectable {




    companion object {
        const val KEY_VERSION = "version"
        const val KEY_CREDITS = "credits"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)

        var appVersion = ""

        try {
            val pInfo = activity!!.packageManager.getPackageInfo("su.tagir.apps.radiot", 0)
            appVersion = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }
        val version: Preference? = findPreference(KEY_VERSION)
        version?.title = "Версия приложения $appVersion"

        findPreference<Preference>(KEY_CREDITS)?.setOnPreferenceClickListener {
//            mainViewModel.navigateToCredits()
            return@setOnPreferenceClickListener true
        }

    }

}