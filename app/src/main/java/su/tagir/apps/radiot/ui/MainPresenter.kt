package su.tagir.apps.radiot.ui

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.Screens
import su.tagir.apps.radiot.model.repository.EntryRepository
import su.tagir.apps.radiot.ui.mvp.BasePresenter
import su.tagir.apps.radiot.ui.mvp.MainDispatcher
import java.util.*
import kotlin.math.floor

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

    private fun convertTime(nextShowTime: Long): String {
        val timeMoscow = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow"))

        val totalSeconds = floor((nextShowTime - timeMoscow.timeInMillis) * 0.001).toInt()

        if (totalSeconds < 0) {
            return "Вещаем!"
        }

        val days = totalSeconds / (24 * 3600)
        var seconds = totalSeconds % (24 * 3600)
        val hours = seconds / 3600
        seconds %= 3600
        val minutes = seconds / 60
        seconds %= 60

        var result = "До эфира\n"
        if (days > 0) {
            result += "$days д. "
        }
        if (hours > 0) {
            result += String.format("%02d", hours) + " ч. "
        }
        if (minutes > 0) {
            result += String.format("%02d", minutes) + " м. "
        }
        result += String.format("%02d ", seconds) + " с."

        return result
    }
}