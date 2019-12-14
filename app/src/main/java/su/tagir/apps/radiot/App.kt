package su.tagir.apps.radiot

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import saschpe.android.customtabs.CustomTabsActivityLifecycleCallbacks
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.di.AppModule
import su.tagir.apps.radiot.di.DataModule
import su.tagir.apps.radiot.di.NavigationModule
import su.tagir.apps.radiot.ui.notification.createNotificationsChannels
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import timber.log.Timber

class App : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = AppComponent.Impl(appModule = AppModule.Impl(this),
                dataModule = DataModule.Impl(this),
                navigationModule = NavigationModule.Impl())

//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        initTools()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationsChannels(this)
        }

        registerActivityLifecycleCallbacks(CustomTabsActivityLifecycleCallbacks())
        setNightMode()
    }

    private fun setNightMode() {
        val prefs = appComponent.preferences
        val modes = resources.getStringArray(R.array.night_mode)
        val mode = prefs.getString(SettingsFragment.KEY_NIGHT_MODE, modes[0])
        when (mode) {
            modes[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            modes[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

    }


    private fun initTools() {

        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build()

        Fabric.with(this, crashlyticsKit)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)

//            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()
//                    .penaltyLog()
//                    .build())
//
//            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .build())

            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree(PreferenceManager.getDefaultSharedPreferences(this)))
        }
    }


    private class CrashReportingTree(private val prefs: SharedPreferences) : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            val report = prefs.getBoolean(SettingsFragment.KEY_CRASH_REPORTS, true)

            if (!report) {
                return
            }

            Crashlytics.log(priority, tag, message)
            if (t != null) {
                if (priority == Log.ERROR) {
                    Crashlytics.logException(t)
                }
            }
        }
    }
}