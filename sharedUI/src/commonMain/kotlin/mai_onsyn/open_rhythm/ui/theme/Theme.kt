package mai_onsyn.open_rhythm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.modules.dialog.LocalPopupHostState
import mai_onsyn.open_rhythm.ui.modules.dialog.PopupHostState
import mai_onsyn.open_rhythm.ui.modules.dialog.RootPopupHost

fun interpolateColorScheme(
    from: ColorScheme,
    to: ColorScheme,
    fraction: Float   // 0f ~ 1f
): ColorScheme {
    return ColorScheme(
        primary = lerp(from.primary, to.primary, fraction),
        onPrimary = lerp(from.onPrimary, to.onPrimary, fraction),
        primaryContainer = lerp(from.primaryContainer, to.primaryContainer, fraction),
        onPrimaryContainer = lerp(from.onPrimaryContainer, to.onPrimaryContainer, fraction),
        inversePrimary = lerp(from.inversePrimary, to.inversePrimary, fraction),
        secondary = lerp(from.secondary, to.secondary, fraction),
        onSecondary = lerp(from.onSecondary, to.onSecondary, fraction),
        secondaryContainer = lerp(from.secondaryContainer, to.secondaryContainer, fraction),
        onSecondaryContainer = lerp(from.onSecondaryContainer, to.onSecondaryContainer, fraction),
        tertiary = lerp(from.tertiary, to.tertiary, fraction),
        onTertiary = lerp(from.onTertiary, to.onTertiary, fraction),
        tertiaryContainer = lerp(from.tertiaryContainer, to.tertiaryContainer, fraction),
        onTertiaryContainer = lerp(from.onTertiaryContainer, to.onTertiaryContainer, fraction),
        background = lerp(from.background, to.background, fraction),
        onBackground = lerp(from.onBackground, to.onBackground, fraction),
        surface = lerp(from.surface, to.surface, fraction),
        onSurface = lerp(from.onSurface, to.onSurface, fraction),
        surfaceVariant = lerp(from.surfaceVariant, to.surfaceVariant, fraction),
        onSurfaceVariant = lerp(from.onSurfaceVariant, to.onSurfaceVariant, fraction),
        surfaceTint = lerp(from.surfaceTint, to.surfaceTint, fraction),
        inverseSurface = lerp(from.inverseSurface, to.inverseSurface, fraction),
        inverseOnSurface = lerp(from.inverseOnSurface, to.inverseOnSurface, fraction),
        error = lerp(from.error, to.error, fraction),
        onError = lerp(from.onError, to.onError, fraction),
        errorContainer = lerp(from.errorContainer, to.errorContainer, fraction),
        onErrorContainer = lerp(from.onErrorContainer, to.onErrorContainer, fraction),
        outline = lerp(from.outline, to.outline, fraction),
        outlineVariant = lerp(from.outlineVariant, to.outlineVariant, fraction),
        scrim = lerp(from.scrim, to.scrim, fraction),
        surfaceBright = lerp(from.surfaceBright, to.surfaceBright, fraction),
        surfaceDim = lerp(from.surfaceDim, to.surfaceDim, fraction),
        surfaceContainer = lerp(from.surfaceContainer, to.surfaceContainer, fraction),
        surfaceContainerHigh = lerp(from.surfaceContainerHigh, to.surfaceContainerHigh, fraction),
        surfaceContainerHighest = lerp(from.surfaceContainerHighest, to.surfaceContainerHighest, fraction),
        surfaceContainerLow = lerp(from.surfaceContainerLow, to.surfaceContainerLow, fraction),
        surfaceContainerLowest = lerp(from.surfaceContainerLowest, to.surfaceContainerLowest, fraction),
        primaryFixed = lerp(from.primaryFixed, to.primaryFixed, fraction),
        primaryFixedDim = lerp(from.primaryFixedDim, to.primaryFixedDim, fraction),
        onPrimaryFixed = lerp(from.onPrimaryFixed, to.onPrimaryFixed, fraction),
        onPrimaryFixedVariant = lerp(from.onPrimaryFixedVariant, to.onPrimaryFixedVariant, fraction),
        secondaryFixed = lerp(from.secondaryFixed, to.secondaryFixed, fraction),
        secondaryFixedDim = lerp(from.secondaryFixedDim, to.secondaryFixedDim, fraction),
        onSecondaryFixed = lerp(from.onSecondaryFixed, to.onSecondaryFixed, fraction),
        onSecondaryFixedVariant = lerp(from.onSecondaryFixedVariant, to.onSecondaryFixedVariant, fraction),
        tertiaryFixed = lerp(from.tertiaryFixed, to.tertiaryFixed, fraction),
        tertiaryFixedDim = lerp(from.tertiaryFixedDim, to.tertiaryFixedDim, fraction),
        onTertiaryFixed = lerp(from.onTertiaryFixed, to.onTertiaryFixed, fraction),
        onTertiaryFixedVariant = lerp(from.onTertiaryFixedVariant, to.onTertiaryFixedVariant, fraction)
    )
}

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AppTheme(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val isDark = remember(Singleton.settings.DarkMode, systemIsDark) {
        Logger.d { "Change darkmode to ${Singleton.settings.DarkMode}, system isDark: $systemIsDark" }
        when (Singleton.settings.DarkMode) {
            0 -> false
            1 -> true
            else -> systemIsDark
        }
    }

    val popupHostState = remember { PopupHostState() }
    CompositionLocalProvider(
        LocalPopupHostState provides popupHostState,
        LocalMinimumInteractiveComponentSize provides 0.dp
    ) {
        onThemeChanged(!isDark)
        DynamicMaterialTheme(
            seedColor = Singleton.settings.PrimarySeedColor,
            isDark = isDark,
            style = if (isDark) PaletteStyle.Content else PaletteStyle.TonalSpot,
            animate = true
        ) {
            RootPopupHost {
                Surface(content = content)
            }
        }
    }
}