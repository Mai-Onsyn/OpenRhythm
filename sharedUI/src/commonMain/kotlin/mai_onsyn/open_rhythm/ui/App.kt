package mai_onsyn.open_rhythm.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.navigation.compose.rememberNavController
import io.github.sudarshanmhasrup.localina.api.LocaleUpdater
import io.github.sudarshanmhasrup.localina.api.LocalinaApp
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.pages.AppNavigation
import mai_onsyn.open_rhythm.ui.theme.AppTheme
import mai_onsyn.open_rhythm.ui.utility.localeOrder
import mai_onsyn.open_rhythm.ui.utility.orderLocale

@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {
    LaunchedEffect(Unit) {
        val localeOrder = localeOrder(Locale.current.language)
        if (Global.settings.Language == -1) {
            Global.settings.Language = if (localeOrder == -1) 0 else localeOrder
        }
        LocaleUpdater.updateLocale(orderLocale(Global.settings.Language))
    }
    val navController = rememberNavController()
    LocalinaApp {
        AppNavigation(
            modifier = Modifier.fillMaxSize(),
            navController = navController
        )
    }
}