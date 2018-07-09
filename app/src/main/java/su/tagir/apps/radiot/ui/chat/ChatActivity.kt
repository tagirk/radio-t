package su.tagir.apps.radiot.ui.chat

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import butterknife.BindColor
import butterknife.ButterKnife
import dagger.android.AndroidInjector
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
import su.tagir.apps.radiot.di.Injectable
import su.tagir.apps.radiot.ui.common.BaseFragment
import javax.inject.Inject

class ChatActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @JvmField
    @BindColor(R.color.colorPrimary)
    var primaryColor: Int = 0


    private lateinit var chatViewModel: ChatViewModel

    private val currentFragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment?


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        ButterKnife.bind(this)
        chatViewModel = ViewModelProviders.of(this, viewModelFactory).get(ChatViewModel::class.java)

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

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    private fun initStartFragment() {
        if (currentFragment == null) {

            val fragmemt: Fragment = if (chatViewModel.isSignedIn) ChatFragment() else AuthFragment()

            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragmemt)
                    .commitNowAllowingStateLoss()
        }
    }

    private val navigator = object : SupportAppNavigator(this, R.id.fragment_container) {
        override fun createActivityIntent(context: Context?, screenKey: String?, data: Any?): Intent? {
            return null
        }

        override fun createFragment(screenKey: String?, data: Any?): Fragment? =
                when (screenKey) {
                    Screens.CHAT_AUTH_SCREEN -> AuthFragment()
                    Screens.CHAT_SCREEN -> ChatFragment()
                    else -> null
                }

        override fun applyCommand(command: Command?) {
            if (command is Forward && command.screenKey == Screens.WEB_SCREEN) {
                openWebPage(command.transitionData as String)
            } else {
                super.applyCommand(command)
            }
        }

        override fun setupFragmentTransactionAnimation(command: Command?, currentFragment: Fragment?, nextFragment: Fragment?, fragmentTransaction: FragmentTransaction?) {
            fragmentTransaction?.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        }

        override fun createStartActivityOptions(command: Command?, activityIntent: Intent?): Bundle? {
            val options = ActivityOptionsCompat.makeCustomAnimation(this@ChatActivity, R.anim.fade_in, R.anim.fade_out)
            return options.toBundle()
        }

        private fun openWebPage(url: String) {
            val customTabsIntent = CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem()
                    .setToolbarColor(primaryColor)
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