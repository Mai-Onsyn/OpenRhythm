package mai_onsyn.open_rhythm.ui.pages

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import mai_onsyn.open_rhythm.ui.pages.free_play_screen.FreePlayPage
import mai_onsyn.open_rhythm.ui.pages.home.HomePage
import mai_onsyn.open_rhythm.ui.pages.library.LibraryPage
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsPage

@Serializable object Home

@Serializable object Library


@Serializable object FreePlayScreen

@Serializable object Setting

@Serializable object PlayScreen

@Serializable object MidiDetail

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    BoxWithConstraints(modifier) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = Library
        ) {
            composable<Home>(
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(animationSpec = tween(300))
                }
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
            composable<Library> {
                LibraryPage(
                    maxWidth > 600.dp,
                    onBack,
                    { navController.navigate(MidiDetail) }
                )
            }

            composable<FreePlayScreen> {
                FreePlayPage(
                    onBack
                )
            }

            composable<Setting> {
                SettingsPage(
                    onBack
                )
            }
        }
    }
}