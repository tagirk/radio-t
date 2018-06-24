package su.tagir.apps.radiot.ui

import android.animation.ValueAnimator
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
import android.os.Handler
import android.os.IBinder
import android.os.RemoteException
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.EntryState.PLAYING
import su.tagir.apps.radiot.model.entries.Progress
import su.tagir.apps.radiot.service.AudioService
import su.tagir.apps.radiot.service.IAudioService
import su.tagir.apps.radiot.service.IAudioServiceCallback
import su.tagir.apps.radiot.ui.chat.ChatActivity
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.localcontent.LocalContentFragment
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesTabsFragment
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.player.PlayerViewModel
import su.tagir.apps.radiot.ui.podcasts.PodcastTabsFragment
import su.tagir.apps.radiot.ui.search.SearchFragment
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import su.tagir.apps.radiot.ui.stream.ArticlesFragment
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
        HasSupportFragmentInjector, ServiceConnection, RadioGroup.OnCheckedChangeListener {

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

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout

    @BindView(R.id.nav_view)
    lateinit var navigationView: NavigationView

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    private lateinit var timeLeft: TextView
    private lateinit var playStream: ImageButton
    private lateinit var pauseStream: ImageButton
    private lateinit var homeDrawable: DrawerArrowDrawable

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
    private lateinit var mainViewModel: MainViewModel

    private val serviceCallback = AudioServiceCallback(this)

    private var audioService: IAudioService? = null

    private val handler = Handler()

    private val currentFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)

    private var isHomeAsUp = false
        set(value) {
            if (field != value) {
                field = value
                val anim = if (value) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
                anim.addUpdateListener { valueAnimator ->
                    val slideOffset = valueAnimator.animatedValue as Float
                    homeDrawable.progress = slideOffset
                }
                anim.interpolator = DecelerateInterpolator()

                anim.duration = 300
                anim.start()
            }
            drawerLayout.setDrawerLockMode(if (value) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        homeDrawable = DrawerArrowDrawable(toolbar.context)
        toolbar.navigationIcon = homeDrawable
        toolbar.setNavigationOnClickListener {
            when {
                drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
                isHomeAsUp -> onBackPressed()
                else -> drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        navigationView.findViewById<RadioGroup>(R.id.nav_items).setOnCheckedChangeListener(this)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(BottomSheetCallback())

        timeLeft = findViewById(R.id.time_left)
        playStream = findViewById(R.id.play)
        playStream.setOnClickListener { playerViewModel.onPlayStreamClick() }
        pauseStream = findViewById(R.id.pause)
        pauseStream.setOnClickListener { playerViewModel.onPauseClick() }

        playerViewModel = getViewModel(PlayerViewModel::class.java)
        mainViewModel = getViewModel(MainViewModel::class.java)

        observe()

        initMainScreen()
        initBottomSheet()
        if (savedInstanceState != null) {
            bottomSheetBehavior.state = savedInstanceState.getInt("state")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.search)?.isVisible = currentFragment is PodcastTabsFragment
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.search -> mainViewModel.navigateToSearch()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("state", bottomSheetBehavior.state)
    }

    override fun onStart() {
        super.onStart()
        bindService()
        mainViewModel.start()
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        try {
            audioService?.unregisterCallback(serviceCallback)
            audioService?.onActivityStopped()
        } catch (e: RemoteException) {
            Timber.e(e)
        }
        unbindService(this)
        mainViewModel.stop()
        super.onStop()
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
        when {
            bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            currentFragment is BackClickHandler -> (currentFragment as BackClickHandler).onBackClick()
            else -> super.onBackPressed()
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        drawerLayout.closeDrawer(GravityCompat.START)
        when (checkedId) {
            R.id.nav_podcats -> mainViewModel.navigateToPodcasts()
            R.id.nav_themes -> mainViewModel.navigateToStream()
            R.id.nav_news -> mainViewModel.navigateToNews()
            R.id.nav_settings -> mainViewModel.navigateToSettings()
            R.id.nav_chat -> mainViewModel.navigateToChat()
            R.id.nav_pirates -> mainViewModel.navigateToPirates()
        }
    }

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
                            showHideBtnStream(entry?.url == STREAM_URL && entry.state == PLAYING)
                            when (entry?.state) {
                                PLAYING -> {
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
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                })

        mainViewModel
                .getCurrentScreen()
                .observe(this, Observer {
                    dismissKeyboard(fragmentContainer.windowToken)
                    when (it) {
                        Screens.SETTINGS_SCREEN -> {
                            isHomeAsUp = true
                            toolbar.setTitle(R.string.settings)
                        }
                        Screens.PODCASTS_SCREEN -> {
                            isHomeAsUp = false
                            toolbar.setTitle(R.string.podcasts)
                        }
                        Screens.NEWS_SCREEN -> {
                            isHomeAsUp = false
                            toolbar.setTitle(R.string.news)
                        }
                        Screens.STREAM_SCREEN -> {
                            isHomeAsUp = false
                            toolbar.setTitle(R.string.themes)
                        }
                        Screens.PIRATES_SCREEN -> {
                            isHomeAsUp = false
                            toolbar.setTitle(R.string.pirates)
                        }
                        Screens.SEARCH_SCREEN -> {
                            isHomeAsUp = true
                            toolbar.title = null
                        }
                        else -> {
                            isHomeAsUp = true
                            toolbar.title = it

                        }
                    }
                })

        mainViewModel.getTimer()
                .observe(this, Observer {
                    timeLeft.text = it
                })

    }

    private fun showHideBtnStream(playing: Boolean) {
        playStream.visibleInvisible(!playing)
        pauseStream.visibleInvisible(playing)
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
                    .replace(R.id.fragment_container, PodcastTabsFragment())
                    .commitNowAllowingStateLoss()
        }
    }

    private fun <T : ViewModel> getViewModel(clazz: Class<T>): T = ViewModelProviders.of(this, viewModelFactory).get(clazz)

    private fun dismissKeyboard(windowToken: IBinder?) {
        val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)

    }

    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {

        override fun createActivityIntent(context: Context?, screenKey: String?, data: Any?): Intent? = when (screenKey) {
            Screens.CHAT_ACTIVITY -> Intent(this@MainActivity, ChatActivity::class.java)
            else -> null
        }

        override fun createFragment(screenKey: String?, data: Any?): Fragment? = when (screenKey) {
            Screens.PODCASTS_SCREEN -> PodcastTabsFragment()
            Screens.STREAM_SCREEN -> ArticlesFragment()
            Screens.NEWS_SCREEN -> NewsFragment()
            Screens.SEARCH_SCREEN -> SearchFragment()
            Screens.CONTENT_SCREEN -> LocalContentFragment.newInstance(data as String)
            Screens.SETTINGS_SCREEN -> SettingsFragment()
            Screens.PIRATES_SCREEN -> PiratesTabsFragment()
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
        val params = fragmentContainer.layoutParams as LinearLayout.LayoutParams
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

