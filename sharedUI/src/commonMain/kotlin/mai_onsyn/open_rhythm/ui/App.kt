package mai_onsyn.open_rhythm.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.zwander.kotlin.file.FileUtils
import kotlinx.io.readByteArray
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.midi_flow.MidiKeyBoard
import mai_onsyn.open_rhythm.ui.midi_flow.MidiWaterFall
import mai_onsyn.open_rhythm.ui.theme.AppTheme

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {
    val density = LocalDensity.current

    val activeKeys = remember { mutableStateMapOf<Int, Color>() }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MidiWaterFall(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            midi = _testOnly_LoadMidi()!!
        )

        var keyboardHeight by remember { mutableStateOf(100.dp) }
        MidiKeyBoard(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardHeight),
            activeKey = activeKeys,
            onPress = { key, velocity ->
                Logger.d { "key: $key pressed, velocity: $velocity" }
                activeKeys[key] = Color(138, 226, 52)
                Singleton.player?.pressKey(key, velocity)
            },
            onRelease = { key ->
                Logger.d { "key: $key released" }
                activeKeys.remove(key)
                Singleton.player?.releaseKey(key)
            },
            onHeightDragged = {
                Logger.v { "Keyboard height adjust: $it" }
                keyboardHeight = with(density) { keyboardHeight - it.toDp() }
            }
        )
    }
}

fun _testOnly_LoadMidi(): Midi? {
    val file = FileUtils.fromString("D:\\Users\\Desktop\\天球の奇蹟.mid", false)
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