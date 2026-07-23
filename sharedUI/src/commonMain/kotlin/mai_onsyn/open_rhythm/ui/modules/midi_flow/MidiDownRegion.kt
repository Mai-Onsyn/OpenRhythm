package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import mai_onsyn.open_rhythm.core.GlobalKeyEventDispatcher
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.NoteEvent
import mai_onsyn.open_rhythm.core.midi.device.MidiInputDevice

@Composable
fun MidiDownRegion(
    modifier: Modifier = Modifier,
    midi: Midi,
    trackColors: List<Color> = emptyList(),
    isPlaying: Boolean = false,
    keyboardRatio: Float = 0f,
    onPlayStateChange: (Boolean) -> Unit = {},
    onProgressChange: (Float) -> Unit = {},
    focusRequester: FocusRequester? = null,
    keyDispatcher: GlobalKeyEventDispatcher? = null,
    midiInputDevice: MidiInputDevice? = null
) {
    val density = LocalDensity.current

    val midiActiveKeys = remember { mutableStateMapOf<Int, Color>() }
    val userActiveKeys = remember { mutableStateMapOf<Int, Color>() }

    var keyboardHeight by remember { mutableStateOf(100.dp) }
    val focusRequester = remember { focusRequester ?: FocusRequester() }

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
                    return@onKeyEvent true
                }
                false
            }
    ) {
        var currentTick by remember { mutableStateOf(0L) }
//        val animatedTick by animateIntAsState(
//            targetValue = currentTick.toInt(),
//            animationSpec = tween(easing = LinearEasing, durationMillis = 300)
//        )

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
                                    break
                                }
                            }

                            if (Singleton.settings.DoubleFingerTapToPlayPause) {
                                if (event.changes.size == 2 && event.changes.first().pressed && event.changes.last().pressed) {
                                    Logger.i { "Double Click Toggle to ${!currentIsPlaying}" }
                                    onPlayStateChange(!currentIsPlaying)
                                }
                            }
                        }
                    }
                }
                .then(
                    if (Singleton.settings.DoubleClickToPlayPause)
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    onPlayStateChange(!currentIsPlaying)
                                }
                            )
                        }
                    else Modifier
                ),
            trackColors = trackColors,
            currTick = currentTick,
            midi = midi,
            hpb = hpb,
            activeNoteOutput = midiActiveKeys,
            onVerticalDragged = { deltaYpx += it }
        )
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
//                Singleton.player.setMidi(midi)
                Singleton.player.onCompleted = { onPlayStateChange(false) }
                Singleton.player.play()
            }
            else Singleton.player.pause()
        }
        LaunchedEffect(isPlaying, midi) {
            while (true) {
                withFrameMillis {
                    if (deltaYpx != 0f && !isPlaying) {
                        val deltaTick = (deltaYpx * midi.ppq / with(density) { hpb.toPx() }).toLong()
                        currentTick = Singleton.player.preciseTick + deltaTick
                        Singleton.player.seek(currentTick)
                    }
                    else currentTick = Singleton.player.preciseTick
                    onProgressChange(currentTick / midi.totalTicks.toFloat())
                    deltaYpx = 0f
//                    if (Singleton.settings.AlwaysFocusMidiRegion) focusRequester.requestFocus()
                }
            }
        }
        LaunchedEffect(midi) {
            Singleton.player.setMidi(midi)
        }
        LaunchedEffect(Unit) {
            midiInputDevice?.clearEvents()
            focusRequester.requestFocus()
            Singleton.player.setMidi(midi)
            while (true) {
                midiInputDevice?.handle {
                    Logger.v { "Midi Input: $it" }
                    when (it) {
                        is NoteEvent -> {
                            if (it.on) {
                                userActiveKeys[it.pitch] = Singleton.settings.KeyboardUserInteractionDisplayColor
                            } else userActiveKeys.remove(it.pitch)
                        }
                    }
                    Singleton.player.sendShortEvent(it.event)
                }
            }
        }
//        DisposableEffect(Unit) {
//            val handler: (KeyEvent) -> Boolean = {
//                if (it.key == Key.Spacebar && it.type == KeyEventType.KeyDown) {
//                    onPlayStateChange(!currentIsPlaying)
//                }
//                false
//            }
//            keyDispatcher?.registerHandler(handler)
//            onDispose {
//                keyDispatcher?.unregisterHandler(handler)
//            }
//        }

        MidiKeyBoard(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardHeight),
            midiActiveKey = midiActiveKeys,
            userActiveKey = userActiveKeys,
            onPress = { key, velocity ->
                userActiveKeys[key] = Singleton.settings.KeyboardUserInteractionDisplayColor
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