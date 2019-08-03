package su.tagir.apps.radiot.ui

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.BindColor
import butterknife.BindDimen
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState.PLAYING
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesFragment
import su.tagir.apps.radiot.ui.player.PlayerContract
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastTabsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsFragment
import su.tagir.apps.radiot.ui.settings.AboutFragment
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
        HasSupportFragmentInjector,
        View.OnClickListener, PlayerContract.Presenter.InteractionListener {

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

    private lateinit var mainViewModel: MainViewModel


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

        val navItems = navigationView.findViewById<LinearLayout>(R.id.nav_items)

        for (i in 0 until navItems.childCount) {
            navItems.getChildAt(i).setOnClickListener(this)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(BottomSheetCallback())

        timeLeft = findViewById(R.id.time_left)
        playStream = findViewById(R.id.play)
//        playStream.setOnClickListener { playerViewModel.onPlayStreamClick() }
        pauseStream = findViewById(R.id.pause)
//        pauseStream.setOnClickListener { playerViewModel.onPauseClick() }

        mainViewModel = getViewModel(MainViewModel::class.java)

        observe()

        initMainScreen()
        initBottomSheet()
        if (savedInstanceState != null) {
            bottomSheetBehavior.state = savedInstanceState.getInt("state")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.search)?.isVisible = currentFragment is PodcastTabsFragment
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.search -> mainViewModel.navigateToSearch()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("state", bottomSheetBehavior.state)
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.start()
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)

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

    override fun onClick(v: View?) {
        drawerLayout.closeDrawer(GravityCompat.START)
        when (v?.id) {
            R.id.nav_podcats -> {
                if (currentFragment !is PodcastsFragment) {
                    mainViewModel.navigateToPodcasts()
                }
            }
            R.id.nav_news -> {
                if (currentFragment !is NewsFragment) {
                    mainViewModel.navigateToNews()
                }
            }
            R.id.nav_settings -> mainViewModel.navigateToSettings()
            R.id.nav_chat -> mainViewModel.navigateToChat()
            R.id.nav_pirates -> {
                if (currentFragment !is PiratesFragment) {
                    mainViewModel.navigateToPirates()
                }
            }
            R.id.nav_about -> {
                if (currentFragment !is AboutFragment) {
                    mainViewModel.navigateToAbout()
                }
            }
        }
    }

    override fun onExpand() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun showCurrent(podcast: Entry?) {
        bottomSheet.visibleGone(podcast != null)
        setBottomMargin(if (podcast != null) peekHeight else 0)
        showHideBtnStream(podcast?.url == STREAM_URL && podcast.state == PLAYING)
    }

    private fun observe() {
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
                        Screens.PIRATES_SCREEN -> {
                            isHomeAsUp = false
                            toolbar.setTitle(R.string.pirates)
                        }
                        Screens.SEARCH_SCREEN -> {
                            isHomeAsUp = true
                            toolbar.title = null
                        }
                        Screens.ABOUT_SCREEN -> {
                            isHomeAsUp = true
                            toolbar.setTitle(R.string.about)
                        }
                        Screens.CREDITS_SCREEN -> {
                            isHomeAsUp = true
                            toolbar.setTitle(R.string.credits)
                        }
                        else -> {
                            isHomeAsUp = true
                            toolbar.title = it

                        }

                    }
                    invalidateOptionsMenu()
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


        override fun applyCommand(command: Command?) {
            if (command is Forward && command.screen is Screens.WebScreen) {
                val screen = command.screen as Screens.WebScreen
                openWebPage(screen.url)
            } else if (command is Forward && command.screen is Screens.ResolveActivityScreen) {
                val screen = command.screen as Screens.ResolveActivityScreen
                openWithResolveActivity(screen.url)
            } else {
                super.applyCommand(command)
            }
        }

        override fun setupFragmentTransaction(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction?) {
            fragmentTransaction?.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        }

        override fun createStartActivityOptions(command: Command?, activityIntent: Intent?): Bundle? {
            val options = ActivityOptionsCompat.makeCustomAnimation(this@MainActivity, R.anim.fade_in, R.anim.fade_out)
            return options.toBundle()
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
        fragmentContainer.setPadding(0, 0, 0, margin)
    }

    inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            (supportFragmentManager
                    .findFragmentById(R.id.bottom_sheet) as? PlayerFragment)?.onSlide(slideOffset)

            dim.alpha = slideOffset
        }
    }

}

