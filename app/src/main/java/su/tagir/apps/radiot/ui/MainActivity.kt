package su.tagir.apps.radiot.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.STREAM_URL
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.model.entries.EntryState.PLAYING
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.mvp.BaseMvpActivity
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesFragment
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsTabsFragment
import su.tagir.apps.radiot.ui.settings.AboutFragment
import su.tagir.apps.radiot.utils.visibleGone
import su.tagir.apps.radiot.utils.visibleInvisible
import javax.inject.Inject

class MainActivity : BaseMvpActivity<MainContract.View, MainContract.Presenter>(), MainContract.View,
        HasSupportFragmentInjector,
        View.OnClickListener,
        FragmentsInteractionListener,
        Injectable {


    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var entryRepository: EntryRepository

    private lateinit var bottomSheet: ViewGroup
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var dim: View
    private lateinit var timeLeft: TextView
    private lateinit var playStream: ImageButton
    private lateinit var pauseStream: ImageButton
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val handler = Handler()

    private val currentFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomSheet = findViewById(R.id.bottom_sheet)
        fragmentContainer = findViewById(R.id.fragment_container)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        dim = findViewById(R.id.dim)
        timeLeft = findViewById(R.id.time_left)
        playStream = findViewById(R.id.play)
        playStream.setOnClickListener { presenter.playStream() }
        pauseStream = findViewById(R.id.pause)
        pauseStream.setOnClickListener { presenter.pause() }

        val navItems = navigationView.findViewById<LinearLayout>(R.id.nav_items)

        for (i in 0 until navItems.childCount) {
            navItems.getChildAt(i).setOnClickListener(this)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.bottomSheetCallback = BottomSheetCallback()

        initMainScreen()
        initBottomSheet()
        if (savedInstanceState != null) {
            bottomSheetBehavior.state = savedInstanceState.getInt("state")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("state", bottomSheetBehavior.state)
    }


    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
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
                    presenter.navigateToPodcasts()
                }
            }
            R.id.nav_news -> {
                if (currentFragment !is NewsFragment) {
                    presenter.navigateToNews()
                }
            }
            R.id.nav_settings -> presenter.navigateToSettings()
            R.id.nav_chat -> presenter.navigateToChat()
            R.id.nav_pirates -> {
                if (currentFragment !is PiratesFragment) {
                    presenter.navigateToPirates()
                }
            }
            R.id.nav_about -> {
                if (currentFragment !is AboutFragment) {
                    presenter.navigateToAbout()
                }
            }
        }
    }

    override fun showDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun lockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun unlockDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun showCurrentPodcast(entry: Entry) {
        bottomSheet.visibleGone(true)
        setBottomMargin(resources.getDimensionPixelSize(R.dimen.player_peek_height))
        showHideBtnStream(entry.url == STREAM_URL && entry.state == PLAYING)
    }

    override fun createPresenter(): MainContract.Presenter =
            MainPresenter(entryRepository, router)

    override fun showTime(time: String) {
        timeLeft.text = time
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
                    .replace(R.id.fragment_container, PodcastsTabsFragment())
                    .commitNowAllowingStateLoss()
        }
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
                    .setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
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

