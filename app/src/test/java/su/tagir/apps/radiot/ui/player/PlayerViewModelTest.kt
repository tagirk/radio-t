package su.tagir.apps.radiot.ui.player

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import ru.terrakok.cicerone.Router
import su.tagir.apps.radiot.model.repository.EntryRepository

@RunWith(JUnit4::class)
class PlayerViewModelTest {

    @Mock
    private lateinit var entryRepository: EntryRepository

    @Mock
    private lateinit var router: Router

    private lateinit var playerViewModel: PlayerViewModel


}