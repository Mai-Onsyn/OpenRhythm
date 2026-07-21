package mai_onsyn.open_rhythm.ui.pages.play_screen

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.bpmAtTick
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_back
import mai_onsyn.open_rhythm.ui.icons.ic_arrow_warm_up
import mai_onsyn.open_rhythm.ui.modules.FlatSlider
import mai_onsyn.open_rhythm.ui.modules.MorphingPlayPauseButton
import mai_onsyn.open_rhythm.ui.modules.NumberSpinner
import mai_onsyn.open_rhythm.ui.modules.OpacitySurface

@Composable
fun StatusBar(
    modifier: Modifier = Modifier,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressChangeStart: () -> Unit = {},
    onProgressChangeEnd: () -> Unit = {},
    isPlaying: Boolean,
    onToggledPlay: (Boolean) -> Unit,
    onHide: () -> Unit,
    onBack: () -> Unit,
    speed: Int = 100,
    onSpeedChange: (Int) -> Unit = {},
    bpm: Int = 120,
    focusRequester: FocusRequester? = null,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp)
                .height(72.dp)
        ) {
            IconButton(
                onClick = {
                    onBack()
                    focusRequester?.requestFocus()
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .focusable(false)
            ) {
                Icon(
                    imageVector = ic_arrow_back,
                    contentDescription = "back",
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NumberSpinner(
                        value = speed,
                        onValueChange = onSpeedChange,
                        range = 10..400,
                        step = 10,
                        label = "$bpm BPM",
                        onAddClick = { focusRequester?.requestFocus() },
                        onSubClick = { focusRequester?.requestFocus() }
                    )
                    IconButton(
                        onClick = { onToggledPlay(!isPlaying) },
                        modifier = Modifier
                            .size(40.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        MorphingPlayPauseButton(
                            isPlaying = isPlaying,
                            fill = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                FlatSlider(
                    modifier = Modifier
                        .height(16.dp)
                        .padding(horizontal = 56.dp)
                        .fillMaxWidth(),
                    progress = progress,
                    onChanged = onProgressChange,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    onChangeStart = onProgressChangeStart,
                    onChangeEnd = onProgressChangeEnd,
                )
            }

            IconButton(
                onClick = {
                    onHide()
                    focusRequester?.requestFocus()
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .focusable(false)
            ) {
                Icon(
                    imageVector = ic_arrow_warm_up,
                    contentDescription = "back",
                )
            }
        }
    }
}