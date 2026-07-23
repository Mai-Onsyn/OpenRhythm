package mai_onsyn.open_rhythm.ui.pages.setting.categories.midi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.atsushieno.ktmidi.MidiOutput
import mai_onsyn.open_rhythm.ui.icons.ic_audio_file
import mai_onsyn.open_rhythm.ui.icons.ic_graphic_eq
import mai_onsyn.open_rhythm.ui.icons.ic_settings_input_svideo
import mai_onsyn.open_rhythm.ui.icons.ic_unknown
import mai_onsyn.open_rhythm.ui.pages.setting.SettingsCard

@Composable
fun MidiSettings() {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MidiInputSettings()
            MidiOutputSettings()
            MidiFileSettings()
        }
    }
}

@Composable
private fun MidiInputSettings() {
    SettingsCard(
        title = "Input",
        icon = ic_settings_input_svideo,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {

    }
}

@Composable
private fun MidiOutputSettings() {
    SettingsCard(
        title = "Output",
        icon = ic_graphic_eq,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {

    }
}

@Composable
private fun MidiFileSettings() {
    SettingsCard(
        title = "File",
        icon = ic_audio_file,
        modifier = Modifier.widthIn(400.dp, 800.dp)
    ) {

    }
}