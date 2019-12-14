package su.tagir.apps.radiot.ui

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.MainDispatcher

class MainPresenter(private val entryRepository: EntryRepository,
                    private val router: Router,
                    dispatcher: CoroutineDispatcher = MainDispatcher()) : BasePresenter<MainContract.View>(dispatcher), MainContract.Presenter {


    override fun doOnAttach(view: MainContract.View) {
        observeCurrentPodcast()
    }

    override fun observeCurrentPodcast() {
        launch {
            entryRepository.getCurrent()
                    .collect { entry ->
                        entry?.let {
                            view?.showCurrentPodcast(it)
                        }
                    }
        }
    }

    override fun navigateToPodcasts() {
        router.newRootScreen(Screens.PdcastsScreen)
    }

    override fun navigateToNews() {
        router.newRootScreen(Screens.NewsScreen)
    }

    override fun navigateToSettings() {
        router.newRootScreen(Screens.SettingsScreen)
    }

    override fun navigateToChat() {
        router.navigateTo(Screens.ChatScreen)
    }

    override fun navigateToPirates() {
        router.newRootScreen(Screens.PiratesScreen)
    }

    override fun navigateToAbout() {
        router.navigateTo(Screens.AboutScreen)
    }

    override fun navigateToCredits() {
        router.navigateTo(Screens.CreditsScreen)
    }


}