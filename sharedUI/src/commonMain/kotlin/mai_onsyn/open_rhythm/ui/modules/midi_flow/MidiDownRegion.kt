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
import androidx.compose.ui.graphics.ImageBitmap
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
import mai_onsyn.open_rhythm.bridge.Singleton
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
        var currentTick by remember { mutableStateOf(0L) }

        val hpb by remember { mutableStateOf(120.dp) }
        var deltaYpx by remember { mutableStateOf(0f) }

        Box(
            Modifier
                .weight(1f)
                .background(Singleton.settings.WaterfallBackgroundColor.let { if (it.isUnspecified) MaterialTheme.colorScheme.surface else it })
        ) {
            val bgImageBytes by produceState<ByteArray?>(null, Singleton.settings.BackgroundImageDir) {
                val file = PlatformFile(Singleton.settings.BackgroundImageDir)
                withContext(Dispatchers.IO) {
                    value = if (file.exists() && file.isRegularFile()) {
                        file.readBytes()
                    } else null
                }
            }
            bgImageBytes?.let {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(it)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = "background image",
                    modifier = Modifier
                        .alpha(Singleton.settings.BackgroundImageOpacity)
                        .blur(Singleton.settings.BackgroundImageBlurDp.dp)
                        .matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
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
        }

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
//                Singleton.player.setMidi(midi)
                Singleton.player.onCompletion = { onPlayStateChange(false) }
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
//            midiInputDevice?.clearEvents()
            focusRequester.requestFocus()
            Singleton.player.setMidi(midi)
            Singleton.player.seek(midi.startTick.toLong())
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

        MidiKeyBoard(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyboardHeight),
            midiActiveKey = midiActiveKeys,
            userActiveKey = userActiveKeys,
            onPress = { key, velocity ->
                userActiveKeys[key] = Singleton.settings.KeyboardUserInteractionDisplayColor
                Singleton.player.noteOn(key, velocity)
            },
            onRelease = { key ->
                userActiveKeys.remove(key)
                Singleton.player.noteOff(key)
            },
            onVerticalDragged = {
                keyboardHeight = max(64.dp, with(density) { keyboardHeight - it.toDp() })
            }
        )
    }
}