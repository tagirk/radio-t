package su.tagir.apps.radiot.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import javax.inject.Inject

class MainViewModel @Inject constructor(private val router: Router): ViewModel() {

    private val currentScreen = MutableLiveData<String>()

    fun setCurrentScreen(screen: String){
        this.currentScreen.value = screen
    }

    fun getCurrentScreen(): LiveData<String> = currentScreen

    fun navigateToPodcasts(){
        router.newRootScreen(Screens.PODCASTS_SCREEN)
    }

    fun navigateToStream(){
        router.newRootScreen(Screens.STREAM_SCREEN)
    }

    fun navigateToNews(){
        router.newRootScreen(Screens.NEWS_SCREEN)
    }

    fun navigateToSettings(){
        router.navigateTo(Screens.SETTINGS_SCREEN)
    }

    fun navigateToChat(){
        router.navigateTo(Screens.CHAT_ACTIVITY)
    }

    fun openWebSite(url:String) {
        router.navigateTo(Screens.WEB_SCREEN, url)
    }

    fun navigateToPirates() {
        router.navigateTo(Screens.PIRATES_SCREEN)
    }

    fun navigateToSearch() {
        router.navigateTo(Screens.SEARCH_SCREEN)
    }

    fun back(){
        router.exit()
    }
}