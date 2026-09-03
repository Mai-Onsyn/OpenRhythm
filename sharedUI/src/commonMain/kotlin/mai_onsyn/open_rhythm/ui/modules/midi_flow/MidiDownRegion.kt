package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.ui.utility.BackgroundImage
import mai_onsyn.open_rhythm.ui.utility.BindInputDeviceEvents

@Composable
fun MidiDownRegion(
    modifier: Modifier = Modifier,
    midi: Midi,
    trackColors: List<Color> = emptyList(),
    isPlaying: Boolean = false,
    keyboardRatio: Float = 0f,
    onPlayStateChange: (Boolean) -> Unit = {},
    onProgressChange: (Float) -> Unit = {},
    focusRequester: FocusRequester? = null
) {
    val density = LocalDensity.current

    val midiActiveKeys = remember { mutableStateMapOf<Int, Color>() }
    val userActiveKeys = remember { mutableStateMapOf<Int, Color>() }

    var keyboardHeight by remember { mutableStateOf(100.dp) }
    val focusRequester = remember { focusRequester ?: FocusRequester() }

    val currentIsPlaying by rememberUpdatedState(isPlaying)

    Box(
        Modifier.background(Global.settings.WaterfallBackgroundColor.let { if (it.isUnspecified) MaterialTheme.colorScheme.surface else it })
    ) {
        if (Global.settings.ImageExpandToKeyboard) BackgroundImage()
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
                                break
                            }
                        }
                    }
                }
                .onKeyEvent {
                    if (it.key == Key.Spacebar && it.type == KeyEventType.KeyDown) {
                        Logger.d { "Space bar pressed, switch playback state to ${!currentIsPlaying}" }
                        onPlayStateChange(!currentIsPlaying)
                        return@onKeyEvent true
                    }
                    false
                }
        ) {
            var currentTick by remember { mutableStateOf(0.0) }

            val hpb by remember(Global.settings.QuarterNoteDpHeight) { mutableStateOf(Global.settings.QuarterNoteDpHeight.dp) }
            var deltaYpx by remember { mutableStateOf(0f) }

            Box(Modifier.weight(1f)) {
                if (!Global.settings.ImageExpandToKeyboard) BackgroundImage()
                MidiWaterFall(
                    modifier = Modifier
                        .fillMaxSize()
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

                                    if (Global.settings.DoubleFingerTapToPlayPause) {
                                        if (
                                            event.changes.size == 2 &&
                                            event.changes.first().pressed &&
                                            event.changes.last().pressed &&
                                            event.type == PointerEventType.Press
                                        ) {
                                            Logger.i { "Double finger tap toggle playback state to ${!currentIsPlaying}" }
                                            onPlayStateChange(!currentIsPlaying)
                                        }
                                    }
                                }
                            }
                        }
                        .then(
                            if (Global.settings.DoubleClickToPlayPause)
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            Logger.i { "Double click toggle playback state to ${!currentIsPlaying}" }
                                            onPlayStateChange(!currentIsPlaying)
                                        }
                                    )
                                }
                            else Modifier
                        ),
                    trackColors = trackColors,
                    currTick = currentTick,
                    minPitch = Global.settings.MinPitch,
                    maxPitch = Global.settings.MaxPitch,
                    midi = midi,
                    hpb = hpb,
                    activeNoteOutput = midiActiveKeys,
                    onVerticalDragged = { deltaYpx += it },
                    drawOctaveLine = Global.settings.DrawOctaveLines,
                    drawSectionLine = Global.settings.DrawSectionLines,
                    noteRoundPercent = Global.settings.NoteRoundConerPercent,
                    drawPitchLabel = Global.settings.DrawPitchLabels
                )
            }

            LaunchedEffect(isPlaying, midi, hpb) {
                while (true) {
                    withFrameMillis {
                        if (deltaYpx != 0f && !isPlaying) {
                            val deltaTick = deltaYpx * midi.ppq / with(density) { hpb.toPx() }
                            currentTick = Global.player.preciseTick + deltaTick
                            Global.player.seek(currentTick, false)
                        } else currentTick = Global.player.preciseTick
                        onProgressChange((currentTick / midi.totalTicks).toFloat())
                        deltaYpx = 0f
                    }
                }
            }
            LaunchedEffect(midi) {
                focusRequester.requestFocus()
                Global.player.setMidi(midi, Global.settings.midiFileSettings[midi.path])
                Global.player.seek(midi.startTick.toLong() - midi.ppq * Global.settings.PlaybackStartDistance)
                Logger.i { "Set player midi to $midi" }
            }
            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    Global.player.onCompletion = { onPlayStateChange(false) }
                    Global.player.play()
                } else Global.player.pause()
            }
            BindInputDeviceEvents(userActiveKeys)

            val appendTextMap = appliedOverlayLabels()

            AppDefaultMidiKeyboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyboardHeight),
                midiActiveKey = midiActiveKeys,
                userActiveKey = userActiveKeys,
                onPress = { key, velocity ->
                    userActiveKeys[key] = Global.settings.MidiInteractionColor
                    Global.player.noteOn(key, velocity)
                    if (Global.player.practiceMode) {
                        Global.player.blocker.press(key)
                    }
                },
                onRelease = { key ->
                    userActiveKeys.remove(key)
                    Global.player.noteOff(key)
                    if (Global.player.practiceMode) {
                        Global.player.blocker.release(key)
                    }
                },
                onVerticalDragged = {
                    keyboardHeight = max(64.dp, with(density) { keyboardHeight - it.toDp() })
                },
                appendTexts = appendTextMap
            )
        }
    }
}