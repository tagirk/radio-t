package su.tagir.apps.radiot.ui.settings

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.MainViewModel
import timber.log.Timber
import javax.inject.Inject

class AboutFragment : PreferenceFragmentCompat(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var mainViewModel: MainViewModel


    companion object {
        const val KEY_VERSION = "version"
        const val KEY_CREDITS = "credits"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainViewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)

        var appVersion = ""

        try {
            val pInfo = activity!!.packageManager.getPackageInfo("su.tagir.apps.radiot", 0)
            appVersion = pInfo.versionName;
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
        }
        val version = findPreference(KEY_VERSION)
        version.title = "Версия приложения $appVersion"

        findPreference(KEY_CREDITS).setOnPreferenceClickListener {
            mainViewModel.navigateToCredits()
            return@setOnPreferenceClickListener true
        }

    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setCurrentScreen(Screens.ABOUT_SCREEN)
    }
}