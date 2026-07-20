package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi

@Composable
fun MidiDownRegion(
    modifier: Modifier = Modifier,
    midi: Midi,
    trackColors: List<Color> = emptyList(),
    isPlaying: Boolean = false,
    keyboardRatio: Float = 0f,
    onPlayStateChange: (Boolean) -> Unit = {},
    onProgressChange: (Float) -> Unit = {},
) {
    val density = LocalDensity.current

    val midiActiveKeys = remember { mutableStateMapOf<Int, Color>() }
    val userActiveKeys = remember { mutableStateMapOf<Int, Color>() }

    var keyboardHeight by remember { mutableStateOf(100.dp) }
    val focusRequester = remember { FocusRequester() }

    val currentIsPlaying by rememberUpdatedState(isPlaying)
    Column(
        modifier = modifier
            .focusable()
            .focusRequester(focusRequester)
            .onSizeChanged {
                if (keyboardRatio == 0f) return@onSizeChanged

                keyboardHeight = with(density) { (it.width / keyboardRatio).toDp() }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    for (change in event.changes) {
                        if (change.pressed) {
                            focusRequester.requestFocus()
                            Logger.d { "Column request focus" }
                            break
                        }
                    }
                }
            }
            .onKeyEvent {
                if (it.key == Key.Spacebar && it.type == KeyEventType.KeyDown) {
                    onPlayStateChange(!currentIsPlaying)
                }
                if (it.type != KeyEventType.KeyDown && it.type != KeyEventType.KeyUp) {
                    return@onKeyEvent false
                }

                val userKeys = arrayOf(Key.A, Key.W, Key.S, Key.E, Key.D, Key.F, Key.T, Key.G, Key.Y, Key.H, Key.U, Key.J, Key.K, Key.O, Key.L, Key.P, Key.Semicolon, Key.Apostrophe)
                val idx = userKeys.indexOf(it.key)
                if (idx != -1) {
                    val midiKey = idx + 60
                    if (it.type == KeyEventType.KeyDown) {
                        if (!userActiveKeys.contains(midiKey)) {
                            userActiveKeys[midiKey] = Singleton.settings.keyboardUserInteractionDisplayColor
                            Singleton.player.pressKey(midiKey, 100)
                        }
                    } else {
                        userActiveKeys.remove(midiKey)
                        Singleton.player.releaseKey(midiKey)
                    }
                    return@onKeyEvent true
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
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)

                            for (change in event.changes) {
                                if (change.pressed) {
                                    focusRequester.requestFocus()
//                                    Logger.d { "Waterfall request focus" }
                                    break
                                }
                            }

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
                        Singleton.player.seek(currentTick)
                    }
                    else currentTick = Singleton.player.precisTick
                    onProgressChange(currentTick / midi.totalTicks.toFloat())
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