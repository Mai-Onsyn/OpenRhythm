package mai_onsyn.open_rhythm.ui.pages

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.pages.free_play_screen.FreePlayPage
import mai_onsyn.open_rhythm.ui.pages.home.HomePage
import mai_onsyn.open_rhythm.ui.pages.library.LibraryPage
import mai_onsyn.open_rhythm.ui.pages.library.MidiPlayMethod
import mai_onsyn.open_rhythm.ui.pages.library.loadMidiFile
import mai_onsyn.open_rhythm.ui.pages.play_screen.PlayPage
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsPage

@Serializable object Home

@Serializable object Library


@Serializable object FreePlayScreen

@Serializable object Setting

@Serializable object PlayScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    var currentPlayScreenMidi by remember { mutableStateOf<Midi?>(null) }

    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = Setting
        ) {
            composable<Home>() {
                HomePage(
                    maxWidth > 600.dp,
                    { navController.navigate(Library) },
                    { navController.navigate(FreePlayScreen) },
                    { navController.navigate(Setting) },
                    { TODO("Exit the application") }
                )
            }

            val onBack = {
                if (!navController.popBackStack())
                    navController.navigate(Home)
            }
            composable<Library> {
                LibraryPage(
                    maxWidth > 600.dp,
                    onBack,
                    {
                        scope.launch(Dispatchers.IO) {
                            currentPlayScreenMidi = loadMidiFile(it.data.path)
                        }
                        if (it.playMode == MidiPlayMethod.PlayMode.AUTO) {
                            navController.navigate(PlayScreen)
                        }
                        Logger.i { "Enter Play Screen $it" }
                    }
                )
            }

            composable<PlayScreen> {
                PlayPage(currentPlayScreenMidi, onBack)
            }

            composable<FreePlayScreen> {
                FreePlayPage(
                    onBack
                )
            }

            composable<Setting> {
                SettingsPage(
                    maxWidth > 600.dp,
                    onBack
                )
            }
        }
    }
}