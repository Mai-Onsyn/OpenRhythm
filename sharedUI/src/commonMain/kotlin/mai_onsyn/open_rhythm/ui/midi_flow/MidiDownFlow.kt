package mai_onsyn.open_rhythm.ui.midi_flow

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import kotlin.collections.set

@Composable
fun MidiDownFlow(
    modifier: Modifier = Modifier,
    midi: Midi,
    trackColors: List<Color> = emptyList(),
    isPlaying: Boolean = false,
    keyboardRatio: Float = 0f,
    onPlayStateChange: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current

    val midiActiveKeys = remember { mutableStateMapOf<Int, Color>() }
    val userActiveKeys = remember { mutableStateMapOf<Int, Color>() }

    var keyboardHeight by remember { mutableStateOf(100.dp) }
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = modifier
            .focusable()
            .focusRequester(focusRequester)
            .onSizeChanged {
                if (keyboardRatio == 0f) return@onSizeChanged

                keyboardHeight = with(density) { (it.width / keyboardRatio).toDp() }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { focusRequester.requestFocus() }
                )
            }
            .onKeyEvent {
                if (it.key == Key.Spacebar && it.type == KeyEventType.KeyDown) {
                    onPlayStateChange(!isPlaying)
                }
                false
            }
    ) {
        var currentTick by remember { mutableStateOf(0L) }

        val hpb by remember { mutableStateOf(120.dp) }
        var deltaYpx by remember { mutableStateOf(0f) }
        MidiWaterFall(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            trackColors = trackColors,
            currTick = currentTick,
            midi = midi,
            hpb = hpb,
            activeNoteOutput = midiActiveKeys,
            onVerticalDragged = { deltaY, deltaTick ->
//                if (!currentIsPlaying) Singleton.player?.seek(currentTick + deltaTick)
                deltaYpx += deltaY
            }
        )
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                Singleton.player?.setMidi(midi)
                Singleton.player?.play()
            }
            else Singleton.player?.pause()
        }
        LaunchedEffect(isPlaying) {
            while (true) {
                withFrameMillis {
                    if (deltaYpx != 0f && !isPlaying) {
                        val deltaTick = (deltaYpx * midi.ppq / with(density) { hpb.toPx() }).toLong()
                        currentTick = (Singleton.player?.precisTick ?: 0L) + deltaTick
                        Singleton.player?.seek(currentTick)
                    }
                    else currentTick = Singleton.player?.precisTick ?: 0L
                    deltaYpx = 0f
                }
            }
        }

        MidiKeyBoard(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardHeight),
            midiActiveKey = midiActiveKeys,
            userActiveKey = userActiveKeys,
            onPress = { key, velocity ->
                userActiveKeys[key] = Singleton.settings.keyboardUserInteractionDisplayColor
                Singleton.player?.pressKey(key, velocity)
            },
            onRelease = { key ->
                userActiveKeys.remove(key)
                Singleton.player?.releaseKey(key)
            },
            onVerticalDragged = {
                keyboardHeight = with(density) { keyboardHeight - it.toDp() }
            }
        )
    }
}