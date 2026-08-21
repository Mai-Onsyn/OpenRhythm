package mai_onsyn.open_rhythm.ui.pages.free_play_screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.keepScreenOn
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiUpRegion

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FreePlayPage(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    MidiUpRegion(
        modifier = Modifier
            .fillMaxSize()
            .keepScreenOn(),
        keyboardRatio = Global.settings.KeyboardAspectRatio
    )
}