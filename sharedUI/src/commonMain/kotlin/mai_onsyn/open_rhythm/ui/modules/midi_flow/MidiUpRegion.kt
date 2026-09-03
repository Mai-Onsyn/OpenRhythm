package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.util.Time
import mai_onsyn.open_rhythm.ui.utility.BackgroundImage
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents

@Composable
fun MidiUpRegion(
    modifier: Modifier = Modifier,
    keyboardRatio: Float = 0f,
) {
    val density = LocalDensity.current
    var keyboardHeight by remember { mutableStateOf(100.dp) }
    Box(
        Modifier.background(Global.settings.WaterfallBackgroundColor.let { if (it.isUnspecified) MaterialTheme.colorScheme.surface else it })
    ) {
        if (Global.settings.ImageExpandToKeyboard) BackgroundImage()
        Column(
            modifier = modifier
                .onSizeChanged {
                    if (keyboardRatio == 0f) return@onSizeChanged
                    keyboardHeight = with(density) { (it.width / keyboardRatio).toDp() }
                }
        ) {
            val activeKeys = remember { mutableStateMapOf<Int, Color>() }
            val notes = remember { mutableStateListOf<LiveNote>() }

            Box(Modifier.weight(1f)) {
                if (!Global.settings.ImageExpandToKeyboard) BackgroundImage()
                MidiUpFlow(
                    modifier = Modifier
                        .fillMaxSize(),
                    notes = notes,
                    minPitch = Global.settings.MinPitch,
                    maxPitch = Global.settings.MaxPitch,
                    color = Global.settings.MidiInteractionColor,
                    drawOctaveLine = Global.settings.DrawOctaveLines,
                    noteRoundPercent = Global.settings.NoteRoundConerPercent
                )
            }

            val appendTextMap = appliedOverlayLabels()

            AppDefaultMidiKeyboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyboardHeight),
                userActiveKey = activeKeys,
                onPress = { key, velocity ->
                    activeKeys[key] = Global.settings.MidiInteractionColor
                    Global.player.noteOn(key, velocity)

                    notes.add(LiveNote(key, Time.nanos))
                },
                onRelease = { key ->
                    activeKeys.remove(key)
                    Global.player.noteOff(key)

                    notes.firstOrNull { it.pitch == key && it.endNanos == null }?.let {
                        it.endNanos = Time.nanos
                    }
                },
                onVerticalDragged = {
                    keyboardHeight = max(64.dp, with(density) { keyboardHeight - it.toDp() })
                },
                appendTexts = appendTextMap
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
}