package mai_onsyn.open_rhythm.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mai_onsyn.open_rhythm.ui.pages.AppNavigation
import mai_onsyn.open_rhythm.ui.theme.AppTheme

@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {
    AppNavigation(
        modifier = Modifier.fillMaxSize()
    )
}