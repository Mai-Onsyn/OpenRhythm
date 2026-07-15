package mai_onsyn.open_rhythm.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import co.touchlab.kermit.Logger
import dev.zwander.kotlin.file.FileUtils
import kotlinx.coroutines.delay
import kotlinx.io.readByteArray
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.midi_flow.MidiDownFlow
import mai_onsyn.open_rhythm.ui.theme.AppTheme
import kotlin.random.Random

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {

    val midi = remember { _testOnly_LoadMidi() }
    val trackColors = remember { _testOnly_GenerateTrackColors() }

    var isPlaying by remember { mutableStateOf(false) }
    MidiDownFlow(
        modifier = Modifier.fillMaxSize(),
        midi = midi!!,
        trackColors = trackColors,
        isPlaying = isPlaying,
        keyboardRatio = if (Singleton.settings.KeyboardAutoAspect) Singleton.settings.KeyboardAspectRatio else 0f,
        onPlayStateChange = { isPlaying = it }
    )

    LaunchedEffect(Unit) {
        delay(1000)
        isPlaying = true
    }
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

fun _testOnly_LoadMidi(): Midi? {
    val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\蓬莱伝説 v1.11.mid", false)
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\天球の奇蹟.mid", false)
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\U.N. Owen.mid", false)
//    val file = FileUtils.fromString("D:\\Users\\Desktop\\Files\\voice\\MIDI\\最终鬼畜妹.mid", false)
    if (file == null) {
        Logger.e { "Cant find test.mid" }
        return null
    }
    val byteArray = file.openInputStream()?.readByteArray()
    if (byteArray == null) {
        Logger.e { "Cant read test.mid" }
        return null
    }

    return Midi.fromFile(file.nameWithoutExtension, byteArray.toList())
}