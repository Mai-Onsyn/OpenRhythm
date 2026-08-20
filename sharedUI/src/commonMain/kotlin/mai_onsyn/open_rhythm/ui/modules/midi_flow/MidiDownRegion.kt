package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Size
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
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
    focusRequester: FocusRequester? = null,
    //    midiInputDevice: MidiInputDevice? = null
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
        var currentTick by remember { mutableStateOf(0.0) }

        val hpb by remember(Global.settings.QuarterNoteDpHeight) { mutableStateOf(Global.settings.QuarterNoteDpHeight.dp) }
        var deltaYpx by remember { mutableStateOf(0f) }

        Box(
            Modifier
                .weight(1f)
                .background(Global.settings.WaterfallBackgroundColor.let { if (it.isUnspecified) MaterialTheme.colorScheme.surface else it })
        ) {
            val platformContext = LocalPlatformContext.current
            val bgImageRequest by produceState<ImageRequest?>(null, Global.settings.BackgroundImageDir) {
                val file = PlatformFile(Global.settings.BackgroundImageDir)
                withContext(Dispatchers.IO) {
                    value = if (file.exists() && file.isRegularFile()) {
                        if (Global.settings.OriginalBackgroundImageSize) ImageRequest.Builder(platformContext)
                            .data(file.readBytes())
                            .size(Size.ORIGINAL)
                            .build()
                        else ImageRequest.Builder(platformContext)
                            .data(file.readBytes())
                            .build()
                    } else null
                }
            }
            AsyncImage(
                model = bgImageRequest,
                contentDescription = "background image",
                modifier = Modifier
                    .alpha(Global.settings.BackgroundImageOpacity)
                    .blur(Global.settings.BackgroundImageBlurDp.dp)
                    .matchParentSize(),
                contentScale = ContentScale.Crop,
            )
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
                                    if (event.changes.size == 2 && event.changes.first().pressed && event.changes.last().pressed) {
                                        Logger.i { "Double Click Toggle to ${!currentIsPlaying}" }
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

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
//                Singleton.player.setMidi(midi)
                Global.player.onCompletion = { onPlayStateChange(false) }
                Global.player.play()
            }
            else Global.player.pause()
        }
        LaunchedEffect(isPlaying, midi, hpb) {
            while (true) {
                withFrameMillis {
                    if (deltaYpx != 0f && !isPlaying) {
                        val deltaTick = deltaYpx * midi.ppq / with(density) { hpb.toPx() }
                        currentTick = Global.player.preciseTick + deltaTick
                        Global.player.seek(currentTick, false)
                    }
                    else currentTick = Global.player.preciseTick
                    onProgressChange((currentTick / midi.totalTicks).toFloat())
                    deltaYpx = 0f
//                    if (Singleton.settings.AlwaysFocusMidiRegion) focusRequester.requestFocus()
                }
            }
        }
        LaunchedEffect(midi) {
            Global.player.setMidi(midi)
            Global.player.seek(midi.startTick.toLong() - midi.ppq * 4)
        }
        LaunchedEffect(Unit) {
//            midiInputDevice?.clearEvents()
            focusRequester.requestFocus()
            Global.player.setMidi(midi)
        }
        BindInputDeviceEvents(userActiveKeys)
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