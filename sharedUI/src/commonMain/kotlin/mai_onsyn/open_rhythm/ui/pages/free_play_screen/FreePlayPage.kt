package mai_onsyn.open_rhythm.ui.pages.free_play_screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiUpRegion

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FreePlayPage(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
//    Text("Free Play Page", style = MaterialTheme.typography.titleLarge)
    MidiUpRegion(
        modifier = Modifier.fillMaxSize(),
        keyboardRatio = Singleton.settings.KeyboardAspectRatio
    )
}