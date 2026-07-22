package mai_onsyn.open_rhythm.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.contextmenu.provider.LocalTextContextMenuToolbarProvider
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.lerp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.modules.dialog.LocalPopupHostState
import mai_onsyn.open_rhythm.ui.modules.dialog.PopupHostState
import mai_onsyn.open_rhythm.ui.modules.dialog.RootPopupHost

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

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

    val animatedFraction by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = tween(300)
    )

    val popupHostState = remember { PopupHostState() }
    CompositionLocalProvider(
        LocalPopupHostState provides popupHostState
    ) {
        onThemeChanged(!isDark)
        MaterialTheme(
            colorScheme = interpolateColorScheme(lightColorScheme(), darkColorScheme(), animatedFraction),
            content = {
                RootPopupHost {
                    Surface(content = content)
                }
            }
        )
    }
}

//fun useSystemDark(): Boolean = Singleton.settings.DarkMode == 2
//fun userSpecifyDark(): Boolean = when (Singleton.settings.DarkMode) {
//    0 -> false
//    1 -> true
//    else -> userSpecifyDark()
//}