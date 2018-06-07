package su.tagir.apps.radiot.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.BindView
import butterknife.ButterKnife
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.EntryState
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.service.AudioService
import su.tagir.apps.radiot.service.IAudioService
import su.tagir.apps.radiot.service.IAudioServiceCallback
import su.tagir.apps.radiot.ui.chat.ChatActivity
import su.tagir.apps.radiot.ui.common.BaseFragment
import su.tagir.apps.radiot.ui.localcontent.LocalContentFragment
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import su.tagir.apps.radiot.ui.search.SearchFragment
import su.tagir.apps.radiot.ui.settings.SettingsActivity
import su.tagir.apps.radiot.utils.visibleGone
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
        HasSupportFragmentInjector, ServiceConnection {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @BindView(R.id.bottom_sheet)
    lateinit var bottomSheet: ViewGroup

    @BindView(R.id.fragment_container)
    lateinit var fragmentContainer: FrameLayout

    @JvmField
    @BindColor(R.color.colorPrimary)
    var primaryColor: Int = 0

    @JvmField
    @BindDimen(R.dimen.player_peek_height)
    var peekHeight: Int = 0

    @BindView(R.id.dim)
    lateinit var dim: View

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var playerViewModel: PlayerViewModel

    private val serviceCallback = AudioServiceCallback(this)

    private var audioService: IAudioService? = null

    private val currentFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment?

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(BottomSheetCallback())

        playerViewModel = getViewModel(PlayerViewModel::class.java)

        observe()

        initMainScreen()
        initBottomSheet()
        if (savedInstanceState != null) {
            bottomSheetBehavior.state = savedInstanceState.getInt("state")
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("state", bottomSheetBehavior.state)
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onStop() {
        super.onStop()
        try {
            audioService?.unregisterCallback(serviceCallback)
            audioService?.onActivityStopped()
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        unbindService(this)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            currentFragment?.onBackPressed() ?: super.onBackPressed()
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onServiceDisconnected(name: ComponentName?) {
        try {
            audioService?.unregisterCallback(serviceCallback)
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        audioService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        audioService = IAudioService.Stub.asInterface(service)
        try {
            audioService?.registerCallback(serviceCallback)
            audioService?.onActivityStarted()
        } catch (e: RemoteException) {
            Timber.e(e)
        }
    }

    private fun observe() {
        playerViewModel
                .getCurrentPodcast()
                .observe(this,
                        Observer { entry ->
                            bottomSheet.visibleGone(entry != null)
                            setBottomMargin(if (entry != null) peekHeight else 0)

                            when (entry?.state) {
                                EntryState.PLAYING -> {
                                    if (audioService == null) {
                                        bindService()
                                    }
                                    playerViewModel.startUpdateProgress()
                                }
                                else -> playerViewModel.stopUpdateProgress()
                            }
                        })

        playerViewModel
                .seekEvent()
                .observe(this,
                        Observer {
                            if (it != null) {
                                try {
                                    audioService?.seekTo(it)
                                } catch (e: RemoteException) {
                                    Timber.e(e)
                                }
                            }
                        })

        playerViewModel
                .requestProgressEvent()
                .observe(this,
                        Observer {
                            val progress = Progress()
                            try {
                                audioService?.getProgress(progress)
                                playerViewModel.setProgress(progress)
                            } catch (e: RemoteException) {
                                Timber.e(e)
                            }
                        })

        playerViewModel
                .expandEvent()
                .observe(this, Observer {
                    Timber.d("expand")
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                })
    }

    private fun initBottomSheet() {
        var playerFr = supportFragmentManager.findFragmentById(R.id.bottom_sheet)
        if (playerFr == null) {
            playerFr = PlayerFragment()

            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.bottom_sheet, playerFr)
                    .commitAllowingStateLoss()
        }
    }

    private fun initMainScreen() {
        if (currentFragment == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MainFragment())
                    .commitNowAllowingStateLoss()
        }
    }

    private fun <T : ViewModel> getViewModel(clazz: Class<T>): T = ViewModelProviders.of(this, viewModelFactory).get(clazz)

    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {

        override fun createActivityIntent(context: Context?, screenKey: String?, data: Any?): Intent? = when (screenKey) {
            Screens.SETTINGS_SCREEN -> Intent(this@MainActivity, SettingsActivity::class.java)
            Screens.CHAT_ACTIVITY -> Intent(this@MainActivity, ChatActivity::class.java)
            else -> null
        }

        override fun createFragment(screenKey: String?, data: Any?): Fragment? = when (screenKey) {
            Screens.MAIN_SCREEN -> MainFragment()
            Screens.SEARCH_SCREEN -> SearchFragment()
            Screens.CONTENT_SCREEN -> LocalContentFragment.newInstance(data as String)
            else -> null
        }

        override fun applyCommand(command: Command?) {
            if (command is Forward && command.screenKey == Screens.WEB_SCREEN) {
                openWebPage(command.transitionData as String)
            } else if (command is Forward && command.screenKey == Screens.RESOLVE_ACTIVITY) {
                openWithResolveActivity(command.transitionData as String)
            } else {
                super.applyCommand(command)
            }
        }

        private fun openWithResolveActivity(url: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(this@MainActivity.packageManager) != null) {
                startActivity(intent)
            } else {
                openWebPage(url)
            }
        }

        private fun openWebPage(url: String) {
            val customTabsIntent = CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(primaryColor)
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back_24dp))
                    .build()

            CustomTabsHelper.addKeepAliveExtra(this@MainActivity, customTabsIntent.intent)

            CustomTabsHelper.openCustomTab(this@MainActivity, customTabsIntent,
                    Uri.parse(url),
                    WebViewFallback())
        }
    }

    private fun setBottomMargin(margin: Int) {
        val params = fragmentContainer.layoutParams as CoordinatorLayout.LayoutParams
        params.bottomMargin = margin
        fragmentContainer.requestLayout()
    }

    private fun bindService() {
        val intent = Intent(application, AudioService::class.java)
        intent.action = IAudioService::class.java.name
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            playerViewModel.onSliding(slideOffset)
            dim.alpha = slideOffset
        }
    }

    class AudioServiceCallback(mainActivity: MainActivity) : IAudioServiceCallback.Stub() {

        private val weakRef = WeakReference(mainActivity)

        override fun onStateChanged(loading: Boolean, state: Int) {
            if (weakRef.get()?.playerViewModel?.isLoading()?.value != loading) {
                weakRef.get()?.playerViewModel?.setLoading(loading)
            }
        }

        override fun onError(error: String?) {
            weakRef.get()?.playerViewModel?.setError(error)
        }
    }
}

