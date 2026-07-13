package mai_onsyn.open_rhythm.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import openrhythm.sharedui.generated.resources.*
import mai_onsyn.open_rhythm.ui.theme.AppTheme
import mai_onsyn.open_rhythm.ui.theme.LocalThemeIsDark
import kotlinx.coroutines.isActive
import mai_onsyn.open_rhythm.ui.midi_flow.MidiKeyBoard
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {

    MidiKeyBoard(
        modifier = Modifier.fillMaxSize()
    )
}
