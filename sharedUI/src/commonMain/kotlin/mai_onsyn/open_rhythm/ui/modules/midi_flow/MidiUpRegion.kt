package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents
import kotlin.collections.set

@Composable
fun MidiUpRegion(
    modifier: Modifier = Modifier,
    keyboardRatio: Float = 0f,
) {
    val density = LocalDensity.current
    var keyboardHeight by remember { mutableStateOf(100.dp) }
    Column(
        modifier = modifier
            .onSizeChanged {
                if (keyboardRatio == 0f) return@onSizeChanged
                keyboardHeight = with(density) { (it.width / keyboardRatio).toDp() }
            },
    ) {
        val activeKeys = remember { mutableStateMapOf<Int, Color>() }
        val notes = remember { mutableStateListOf<LiveNote>() }

        MidiUpFlow(
            modifier = Modifier
                .background(Singleton.settings.WaterfallBackgroundColor.let { if (it.isUnspecified) MaterialTheme.colorScheme.surface else it })
                .fillMaxWidth()
                .weight(1f),
            notes = notes,
            color = Singleton.settings.KeyboardUserInteractionDisplayColor
        )

        MidiKeyBoard(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardHeight),
            userActiveKey = activeKeys,
            onPress = { key, velocity ->
                activeKeys[key] = Singleton.settings.KeyboardUserInteractionDisplayColor
                Singleton.player.noteOn(key, velocity)

                notes.add(LiveNote(key, Time.nanos))
            },
            onRelease = { key ->
                activeKeys.remove(key)
                Singleton.player.noteOff(key)

                notes.firstOrNull { it.pitch == key && it.endNanos == null }?.let {
                    it.endNanos = Time.nanos
                }
            },
            onVerticalDragged = {
                keyboardHeight = max(64.dp, with(density) { keyboardHeight - it.toDp() })
            }
        )

        BindInputDeviceEvents(
            activeKeys,
            { key, _ ->
                notes.add(LiveNote(key, Time.nanos))
            },
            { key ->
                notes.firstOrNull { it.pitch == key && it.endNanos == null }?.let {
                    it.endNanos = Time.nanos
                }
            }
        )
    }
}