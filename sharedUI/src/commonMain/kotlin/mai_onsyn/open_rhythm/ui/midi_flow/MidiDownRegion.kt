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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import kotlin.collections.set

@Composable
fun MidiDownRegion(
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

    val currentIsPlaying by rememberUpdatedState(isPlaying)
    Column(
        modifier = modifier
            .onSizeChanged {
                if (keyboardRatio == 0f) return@onSizeChanged

                keyboardHeight = with(density) { (it.width / keyboardRatio).toDp() }
            }
            .onKeyEvent {
                if (it.key == Key.Spacebar && it.type == KeyEventType.KeyDown) {
                    onPlayStateChange(!currentIsPlaying)
                }
                false
            }
    ) {
        var currentTick by remember { mutableStateOf(0L) }

        val hpb by remember { mutableStateOf(120.dp) }
        var deltaYpx by remember { mutableStateOf(0f) }
        MidiWaterFall(
            modifier = Modifier
                .focusable()
                .focusRequester(focusRequester)
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            focusRequester.requestFocus()

                            if (event.changes.size == 2 && event.changes.first().pressed && event.changes.last().pressed) {
                                Logger.i { "Double Click Toggle to ${!currentIsPlaying}" }
                                onPlayStateChange(!currentIsPlaying)
                            }
                        }
                    }
                },
            trackColors = trackColors,
            currTick = currentTick,
            midi = midi,
            hpb = hpb,
            activeNoteOutput = midiActiveKeys,
            onVerticalDragged = { deltaYpx += it }
        )
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                Singleton.player.setMidi(midi)
                Singleton.player.onCompleted = { onPlayStateChange(false) }
                Singleton.player.play()
            }
            else Singleton.player.pause()
        }
        LaunchedEffect(isPlaying) {
            while (true) {
                withFrameMillis {
                    if (deltaYpx != 0f && !isPlaying) {
                        val deltaTick = (deltaYpx * midi.ppq / with(density) { hpb.toPx() }).toLong()
                        currentTick = Singleton.player.precisTick + deltaTick
                        Logger.i { "${Singleton.player.precisTick}" }
                        Singleton.player.seek(currentTick)
                    }
                    else currentTick = Singleton.player.precisTick
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
                Singleton.player.pressKey(key, velocity)
            },
            onRelease = { key ->
                userActiveKeys.remove(key)
                Singleton.player.releaseKey(key)
            },
            onVerticalDragged = {
                keyboardHeight = with(density) { keyboardHeight - it.toDp() }
            }
        )
    }
}