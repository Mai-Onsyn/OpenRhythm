package mai_onsyn.open_rhythm.ui.pages.play_screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import mai_onsyn.open_rhythm.bridge.Singleton
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.bpmAtTick
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiDownRegion
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayPage(
    midi: Midi?,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val trackColors = remember { _testOnly_GenerateTrackColors() }

    var isPlaying by remember { mutableStateOf(false) }
    val displayMidi by rememberUpdatedState(midi ?: Midi("Empty MIDI", 480, 4800))

    var playProgress by remember { mutableStateOf(0.0f) }
    val focusRequester = remember { FocusRequester() }

    var statusBarVisible by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Press)
                            statusBarVisible = true
                    }
                }
            }
    ) {
        MidiDownRegion(
            modifier = Modifier
                .fillMaxSize(),
            midi = displayMidi,
            trackColors = trackColors,
            isPlaying = isPlaying,
            keyboardRatio = if (Singleton.settings.KeyboardAutoAspect) Singleton.settings.KeyboardAspectRatio else 0f,
            onPlayStateChange = { isPlaying = it; Logger.d { isPlaying.toString() } },
            onProgressChange = { playProgress = it },
            keyDispatcher = Singleton.globalKeyEventDispatcher,
            focusRequester = focusRequester,
            midiInputDevice = Singleton.midiInputDevices.entries.firstOrNull()?.value
        )

        var statusBarBoxHeight by remember { mutableStateOf(0) }
        val statusBarHeight by animateDpAsState(
            targetValue = if (statusBarVisible) 0.dp else with(LocalDensity.current) { -statusBarBoxHeight.toDp() },
            animationSpec = tween(easing = LinearOutSlowInEasing)
        )
        var playSpeed by remember { mutableStateOf(100) }
        StatusBar(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged {
                    statusBarBoxHeight = it.height
                }
                .offset(y = statusBarHeight),
            isPlaying = isPlaying,
            onBack = onBack,
            onToggledPlay = { isPlaying = it },
            onHide = { statusBarVisible = false },
            progress = playProgress,
            onProgressChangeStart = {
                if (isPlaying) {
                    Singleton.player.pause()
                }
            },
            onProgressChange = {
                playProgress = it
                Singleton.player.seek(it.toDouble())
            },
            onProgressChangeEnd = {
                if (isPlaying) {
                    Singleton.player.play()
                }
            },
            speed = playSpeed,
            onSpeedChange = {
                playSpeed = it
                Singleton.player.setSpeed(it / 100f)
            },
            bpm = ((midi?.tempoEvents?.bpmAtTick(Singleton.player.preciseTick) ?: 120.0) * Singleton.player.getSpeed()).roundToInt(),
            focusRequester = focusRequester
        )
    }
}

private fun _testOnly_GenerateTrackColors(): List<Color> = mutableListOf(
    Color(255, 182, 193),
    Color(220, 20, 60),
    Color(255, 105, 180),
    Color(218, 112, 214),
    Color(238, 130, 238),
    Color(255, 0, 255),
    Color(65, 105, 225),
    Color(176, 196, 222),
    Color(240, 248, 255),
    Color(0, 191, 255),
    Color(95, 158, 160),
    Color(0, 206, 209),
    Color(47, 79, 79),
    Color(0, 255, 127),
    Color(0, 128, 0),
    Color(173, 255, 47),
    Color(255, 255, 0),
    Color(128, 128, 0),
    Color(255, 165, 0),
    Color(205, 92, 92),
    Color(128, 0, 0),
).apply {
    this.shuffle(Random(114514))
}