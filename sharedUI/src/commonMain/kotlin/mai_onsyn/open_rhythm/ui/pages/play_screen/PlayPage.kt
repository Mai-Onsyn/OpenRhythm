package mai_onsyn.open_rhythm.ui.pages.play_screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.midi_flow.MidiDownRegion
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayPage(
    midi: Midi?,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val trackColors = remember { _testOnly_GenerateTrackColors() }

    var isPlaying by remember { mutableStateOf(false) }
    val displayMidi by rememberUpdatedState(midi ?: Midi("Empty MIDI", 480, 4800))

    MidiDownRegion(
        modifier = Modifier.fillMaxSize(),
        midi = displayMidi,
        trackColors = trackColors,
        isPlaying = isPlaying,
        keyboardRatio = if (Singleton.settings.KeyboardAutoAspect) Singleton.settings.KeyboardAspectRatio else 0f,
        onPlayStateChange = { isPlaying = it }
    )
}

private fun _testOnly_GenerateTrackColors(): List<Color> = mutableListOf(
    Color(255, 182, 193),
    Color(220, 20, 60),
    Color(255, 105, 180),
    Color(218, 112, 214),
    Color(238, 130, 238),
    Color(255, 0, 255),
    Color(65, 105, 225),
    Color(176, 196, 222),
    Color(240, 248, 255),
    Color(0, 191, 255),
    Color(95, 158, 160),
    Color(0, 206, 209),
    Color(47, 79, 79),
    Color(0, 255, 127),
    Color(0, 128, 0),
    Color(173, 255, 47),
    Color(255, 255, 0),
    Color(128, 128, 0),
    Color(255, 165, 0),
    Color(205, 92, 92),
    Color(128, 0, 0),
).apply {
    this.shuffle(Random(114514))
}