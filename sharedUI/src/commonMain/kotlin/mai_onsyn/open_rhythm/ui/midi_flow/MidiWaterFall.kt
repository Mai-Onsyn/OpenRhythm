package mai_onsyn.open_rhythm.ui.midi_flow

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mai_onsyn.open_rhythm.core.midi.Midi
import mai_onsyn.open_rhythm.core.midi.Note
import mai_onsyn.open_rhythm.ui.utility.blackKeyOffset
import mai_onsyn.open_rhythm.ui.utility.countWhiteKeys
import mai_onsyn.open_rhythm.ui.utility.isBlackKey

@Composable
fun MidiWaterFall(
    modifier: Modifier = Modifier,
    currTick: Long = 0,
    midi: Midi,
    minPitch: Int = 21,     // A0 default
    maxPitch: Int = 108,    // C8 default
    trackColors: Map<Int, Color> = emptyMap(),
    hpb: Dp = 120.dp, // height(dp) per beat
    spacing: Dp = 1.dp,
    blackHorizontalPercentage: Float = 0.75f
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val whiteKeyCount = countWhiteKeys(minPitch, maxPitch)
    val spacingPx = with(density) { spacing.toPx() }

    val gridPos = remember { mutableMapOf<Int, Pair<Float, Float>>() }  // Map<key, Pair<x, width>>

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                gridPos.clear()
                val whiteKeyWidth = (size.width - (whiteKeyCount - 1) * spacingPx) / whiteKeyCount

                for (pitch in minPitch..maxPitch) {
                    if (isBlackKey(pitch)) {
                        val offsetPercent = blackKeyOffset(pitch)
                        val centerX = (countWhiteKeys(minPitch, pitch)) * (whiteKeyWidth + spacingPx) - spacingPx / 2
                        val keyWidth = whiteKeyWidth * blackHorizontalPercentage
                        val left = centerX - keyWidth / 2 + keyWidth * offsetPercent
                        gridPos[pitch] = Pair(left, keyWidth)
                    } else {
                        gridPos[pitch] = Pair(
                            (countWhiteKeys(minPitch, pitch) - 1) * (whiteKeyWidth + spacingPx),
                            whiteKeyWidth
                        )
                    }
                }
            }
    ) {
//        for (pitch in minPitch..maxPitch) {
//            if (!isBlackKey(pitch)) {
//                val bounds = gridPos[pitch] ?: continue
//                drawRect(
//                    color = Color.Yellow,
//                    topLeft = Offset(bounds.first, 0f),
//                    size = Size(bounds.second, size.height)
//                )
//            }
//        }
//        for (pitch in minPitch..maxPitch) {
//            if (isBlackKey(pitch)) {
//                val bounds = gridPos[pitch] ?: continue
//                drawRect(
//                    color = Color.Magenta,
//                    topLeft = Offset(bounds.first, 0f),
//                    size = Size(bounds.second, size.height)
//                )
//            }
//        }
    }
}

private data class SimpleTrack(
    val index: Int,
    val track: List<Note>
)

private fun findKeysInWindow(startTick: Long, endTick: Long, midi: Midi): List<SimpleTrack> {
    val tracks = mutableListOf<SimpleTrack>()
    return tracks
}