package mai_onsyn.open_rhythm.ui.pages

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.take
import mai_onsyn.open_rhythm.ui.pages.free_play_screen.FreePlayPage
import mai_onsyn.open_rhythm.ui.pages.home.HomePage
import mai_onsyn.open_rhythm.ui.pages.library.LibraryPage
import mai_onsyn.open_rhythm.ui.pages.library.MidiPlayMethod
import mai_onsyn.open_rhythm.ui.pages.library.MidiPlayMethod.PlayMode.*
import mai_onsyn.open_rhythm.ui.pages.library.loadMidiFile
import mai_onsyn.open_rhythm.ui.pages.play_screen.PlayPage
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsPage

@Serializable object Home

@Serializable object Library


@Serializable object FreePlayScreen

@Serializable object Setting

@Serializable object PlayScreen

private val enterTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {

    slideInHorizontally(
        initialOffsetX = { (it * 0.08f).toInt() }
    ) + fadeIn() + scaleIn(
        initialScale = 0.96f
    )
}

private val exitTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {

    slideOutHorizontally(
        targetOffsetX = { -(it * 0.04f).toInt() }
    ) + fadeOut() + scaleOut(
        targetScale = 0.98f
    )
}

private val popEnterTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {

    slideInHorizontally(
        initialOffsetX = { -(it * 0.08f).toInt() }
    ) + fadeIn() + scaleIn(
        initialScale = 0.96f
    )
}

private val popExitTransition:
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {

    slideOutHorizontally(
        targetOffsetX = { (it * 0.04f).toInt() }
    ) + fadeOut() + scaleOut(
        targetScale = 0.98f
    )
}

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
            startDestination = Home
        ) {
            composable<Home>(
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition
            ) {
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
            composable<Library>(
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition
            ) {
                LibraryPage(
                    maxWidth > 600.dp,
                    onBack,
                    {
                        scope.launch(Dispatchers.IO) {
                            currentPlayScreenMidi = loadMidiFile(it.data.path)
                            when (it.playMode) {
                                AUTO -> {}
                                PRACTICE -> {
                                    Global.player.practiceMode = true
                                }
                                PRACTICE_SINGLE -> {
                                    Global.player.practiceMode = true
                                    currentPlayScreenMidi = currentPlayScreenMidi!!.take(it.trackNum)
                                }
                            }
                        }
                        navController.navigate(PlayScreen)
                        Logger.i { "Enter Play Screen $it" }
                    }
                )
            }

            composable<PlayScreen>(
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition
            ) {
                PlayPage(currentPlayScreenMidi, onBack)
            }

            composable<FreePlayScreen>(
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition
            ) {
                FreePlayPage(onBack)
            }

            composable<Setting>(
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition
            ) {
                SettingsPage(maxWidth > 600.dp, onBack)
            }
        }
    }
}