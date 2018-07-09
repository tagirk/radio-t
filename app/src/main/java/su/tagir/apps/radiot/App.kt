package su.tagir.apps.radiot

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.ContentProvider
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.evernote.android.job.JobConfig
import com.evernote.android.job.JobManager
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasContentProviderInjector
import dagger.android.HasServiceInjector
import io.fabric.sdk.android.Fabric
import saschpe.android.customtabs.CustomTabsActivityLifecycleCallbacks
import su.tagir.apps.radiot.di.AppInjector
import su.tagir.apps.radiot.job.RadiotJobCreator
import su.tagir.apps.radiot.ui.notification.createNotificationsChannels
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import timber.log.Timber
import javax.inject.Inject

class App : Application(),
        HasActivityInjector,
        HasServiceInjector,
        HasContentProviderInjector{

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var dispatchContentProviderInjector: DispatchingAndroidInjector<ContentProvider>


    override fun activityInjector() = dispatchingActivityInjector
    override fun serviceInjector() = dispatchingServiceInjector
    override fun contentProviderInjector() = dispatchContentProviderInjector

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)

        AppInjector.inject(this)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)


        initTools()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationsChannels(this)
        }

        JobManager
                .create(this)
                .addJobCreator(RadiotJobCreator())

        registerActivityLifecycleCallbacks(CustomTabsActivityLifecycleCallbacks())
        setNightMode()
    }

    private fun setNightMode() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val modes = resources.getStringArray(R.array.night_mode)
        val mode = prefs.getString(SettingsFragment.KEY_NIGHT_MODE, modes[0])
        when (mode) {
            modes[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
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
        JobConfig.setLogcatEnabled(BuildConfig.DEBUG)
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