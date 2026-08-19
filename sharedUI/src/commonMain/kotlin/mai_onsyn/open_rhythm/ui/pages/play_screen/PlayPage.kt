package mai_onsyn.open_rhythm.ui.pages.play_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import mai_onsyn.open_rhythm.bridge.Global
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.util.bpmAtTick
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_warm_up
import mai_onsyn.open_rhythm.ui.modules.midi_flow.MidiDownRegion
import mai_onsyn.open_rhythm.ui.theme.TrackColorDefaults
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayPage(
    midi: Midi?,
    onBack: () -> Unit,
    drawStatusBar: Boolean = true
) {
    if (drawStatusBar) BackHandler { onBack() }
//    val trackColors = remember { _testOnly_GenerateTrackColors() }

    var isPlaying by remember { mutableStateOf(false) }
    val displayMidi by rememberUpdatedState(
        (midi ?: Midi("Empty MIDI", 480, 4800)).apply {
            tracks.forEach { track ->
                if (track.trackChannel == 9) {
                    track.visible = !Global.settings.DrumKitHiddenByDefault
                }
            }
        }
    )

    var playProgress by remember { mutableStateOf(0.0f) }
    val focusRequester = remember { FocusRequester() }

    DisposableEffect(Unit) {
        onDispose {
            Global.player.practiceMode = false
        }
    }

    var statusBarVisible by remember { mutableStateOf(true) }
    var dropdownButtonVisible by remember { mutableStateOf(false) }
    var eventInteractState by remember { mutableStateOf(0L)}
    Box(
        modifier = Modifier
            .clip(RectangleShape)
    ) {
        MidiDownRegion(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press) {
                                dropdownButtonVisible = true
                                eventInteractState++
                            }
                        }
                    }
                },
            midi = displayMidi,
            trackColors = Global.settings.trackColors.let { if (it.isEmpty()) TrackColorDefaults.colors() else it },
            isPlaying = isPlaying,
            keyboardRatio = if (Global.settings.KeyboardAutoAspect) Global.settings.KeyboardAspectRatio else 0f,
            onPlayStateChange = { isPlaying = it; Logger.d { isPlaying.toString() } },
            onProgressChange = { playProgress = it },
            focusRequester = focusRequester
        )

        if (drawStatusBar) {
            AnimatedVisibility(
                visible = dropdownButtonVisible,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(top = 16.dp, end = 16.dp),
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                IconButton(
                    onClick = {
                        statusBarVisible = true
                        dropdownButtonVisible = false
                    },
                    modifier = Modifier.alpha(0.6f).pointerHoverIcon(PointerIcon.Hand),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = ic_arrow_warm_up,
                        contentDescription = "show status bar",
                        modifier = Modifier.rotate(180f)
                    )
                }
            }

            LaunchedEffect(dropdownButtonVisible, eventInteractState) {
                if (dropdownButtonVisible) {
                    delay(2.seconds)
                    dropdownButtonVisible = false
                }
            }

            var statusBarBoxHeight by remember { mutableStateOf(0) }
            val statusBarHeight by animateDpAsState(
                targetValue = if (statusBarVisible) 0.dp else with(LocalDensity.current) { -statusBarBoxHeight.toDp() },
                animationSpec = tween(easing = LinearOutSlowInEasing)
            )
            var playSpeed by remember { mutableStateOf(100) }
            StatusBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { statusBarBoxHeight = it.height }
                    .offset(y = statusBarHeight),
                isPlaying = isPlaying,
                onBack = onBack,
                onToggledPlay = { isPlaying = it },
                onHide = {
                    statusBarVisible = false
                    dropdownButtonVisible = true
                },
                progress = playProgress,
                onProgressChangeStart = {
                    if (isPlaying) {
                        Global.player.pause()
                    }
                },
                onProgressChange = {
                    playProgress = it
                    Global.player.seek(it.toDouble())
                },
                onProgressChangeEnd = {
                    if (isPlaying) {
                        Global.player.play()
                    }
                },
                speed = playSpeed,
                onSpeedChange = {
                    playSpeed = it
                    Global.player.setSpeed(it / 100f)
                },
                bpm = ((midi?.tempoEvents?.bpmAtTick(Global.player.preciseTick.toLong()) ?: 120.0) * Global.player.getSpeed()).roundToInt(),
                focusRequester = focusRequester
            )
        }
    }
}