package su.tagir.apps.radiot.ui.chat

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import su.tagir.apps.radiot.App
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.Screens

class ChatActivity : AppCompatActivity() {

    lateinit var navigatorHolder: NavigatorHolder
        private set

    private val currentFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        navigatorHolder = (application as App).appComponent.navigatorHolder

        initStartFragment()

    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)

    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    private fun initStartFragment() {
        if (currentFragment == null) {

            val fragment: Fragment = ChatFragment()

            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNowAllowingStateLoss()
        }
    }

    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {

        override fun applyCommand(command: Command?) {
            if (command is Forward && command.screen  is Screens.WebScreen) {
                val screen = command.screen  as Screens.WebScreen
                openWebPage(screen.url)
            } else {
                super.applyCommand(command)
            }
        }

        override fun setupFragmentTransaction(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction?) {
            fragmentTransaction?.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        }

        override fun createStartActivityOptions(command: Command?, activityIntent: Intent?): Bundle? {
            val options = ActivityOptionsCompat.makeCustomAnimation(this@ChatActivity, R.anim.fade_in, R.anim.fade_out)
            return options.toBundle()
        }

        private fun openWebPage(url: String) {
            val customTabsIntent = CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(ContextCompat.getColor(this@ChatActivity, R.color.colorPrimary))
                    .setShowTitle(true)
                    .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back_24dp))
                    .build()

            CustomTabsHelper.addKeepAliveExtra(this@ChatActivity, customTabsIntent.intent)

            CustomTabsHelper.openCustomTab(this@ChatActivity, customTabsIntent,
                    Uri.parse(url),
                    WebViewFallback())
        }
    }
}