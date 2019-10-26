package su.tagir.apps.radiot.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.di.AppComponent
import su.tagir.apps.radiot.model.entries.Entry
import su.tagir.apps.radiot.ui.common.BackClickHandler
import su.tagir.apps.radiot.ui.mvp.BaseMvpActivity
import su.tagir.apps.radiot.ui.news.ArticlesFragment
import su.tagir.apps.radiot.ui.news.NewsFragment
import su.tagir.apps.radiot.ui.pirates.PiratesFragment
import su.tagir.apps.radiot.ui.pirates.PiratesTabsFragment
import su.tagir.apps.radiot.ui.player.PlayerFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsFragment
import su.tagir.apps.radiot.ui.podcasts.PodcastsTabsFragment
import su.tagir.apps.radiot.ui.settings.AboutFragment
import su.tagir.apps.radiot.ui.settings.SettingsFragment
import su.tagir.apps.radiot.ui.settings.SettingsFragmentRoot
import su.tagir.apps.radiot.utils.visibleGone
import timber.log.Timber

class MainActivity : BaseMvpActivity<MainContract.View, MainContract.Presenter>(), MainContract.View,
        View.OnClickListener,
        BottomNavigationView.OnNavigationItemSelectedListener {


    private lateinit var navigatorHolder: NavigatorHolder
    private lateinit var bottomSheet: ViewGroup
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var navigationView: BottomNavigationView
    private lateinit var dim: View
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val handler = Handler()

    private val currentFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigatorHolder = (application as App).appComponent.navigatorHolder

        bottomSheet = findViewById(R.id.bottom_sheet)
        fragmentContainer = findViewById(R.id.fragment_container)
        navigationView = findViewById(R.id.navigation_view)
        dim = findViewById(R.id.dim)

        navigationView.setOnNavigationItemSelectedListener(this)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(BottomSheetCallback())

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

    override fun onStart() {
        super.onStart()
        dim.visibleGone(bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED)
        navigationView.visibleGone(bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
        when(currentFragment){
            is PodcastsTabsFragment -> navigationView.selectedItemId = R.id.podcasts
            is ArticlesFragment -> navigationView.selectedItemId = R.id.news
            is PiratesTabsFragment -> navigationView.selectedItemId = R.id.pirates
            is SettingsFragmentRoot -> navigationView.selectedItemId = R.id.settings
        }
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
            currentFragment is BackClickHandler -> (currentFragment as BackClickHandler).onBackClick()
            else -> super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.podcasts -> {
                if (currentFragment !is PodcastsFragment) {
                    presenter.navigateToPodcasts()
                }
            }
            R.id.news -> {
                if (currentFragment !is NewsFragment) {
                    presenter.navigateToNews()
                }
            }
            R.id.settings ->
                if(currentFragment !is SettingsFragment) {
                    presenter.navigateToSettings()
                }
            R.id.chat -> presenter.navigateToChat()
            R.id.pirates -> {
                if (currentFragment !is PiratesFragment) {
                    presenter.navigateToPirates()
                }
            }
        }
        return true
    }

    override fun onClick(v: View?) {
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

    override fun showCurrentPodcast(entry: Entry) {
        bottomSheet.visibleGone(true)
        setBottomMargin(resources.getDimensionPixelSize(R.dimen.player_peek_height))
    }

    override fun createPresenter(): MainContract.Presenter {
        val appComponent: AppComponent = (application as App).appComponent
        val entryRepository = appComponent.entryRepository
        val router = appComponent.router
        return MainPresenter(entryRepository, router)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("onRequestPermissionsResult: $requestCode")
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
            } else if (command is Forward && command.screen is Screens.CommentsScreen) {
                val screen = command.screen as Screens.CommentsScreen
                openWebPage("${screen.entry.url}/#comments")
            }else if (command is Forward && command.screen is Screens.ResolveActivityScreen) {
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
            dim.visibleGone(newState != BottomSheetBehavior.STATE_COLLAPSED)
            navigationView.visibleGone(newState != BottomSheetBehavior.STATE_EXPANDED)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            (supportFragmentManager
                    .findFragmentById(R.id.bottom_sheet) as? PlayerFragment)?.onSlide(slideOffset)

            dim.alpha = slideOffset
            navigationView.alpha = 1 - slideOffset
        }
    }

}

