package su.tagir.apps.radiot.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import timber.log.Timber
import javax.inject.Inject

class AboutFragmentRoot: Fragment(), Injectable{

    @Inject
    lateinit var router: Router


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_toolbar, container, false)
        val toolbar = v.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener { router.exit() }
        toolbar.setTitle(R.string.about)

        if (childFragmentManager.findFragmentById(R.id.container) == null){
            childFragmentManager
                    .beginTransaction()
                    .replace(R.id.container, AboutFragment())
                    .commitAllowingStateLoss()
        }

        return v
    }
}

class AboutFragment : PreferenceFragmentCompat(), Injectable {

    companion object {
        const val KEY_VERSION = "version"
        const val KEY_CREDITS = "credits"
    }

    @Inject
    lateinit var router: Router

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
            router.navigateTo(Screens.CreditsScreen)
            return@setOnPreferenceClickListener true
        }

    }

}