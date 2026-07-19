package mai_onsyn.open_rhythm.ui.modules

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingPlayPauseButton (
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
    fill: Color = Color.White
) {
    val animateProgress by animateFloatAsState(if (isPlaying) 1f else 0f)

    val innerPlaying by rememberUpdatedState(isPlaying)
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggle(!innerPlaying) }
                )
            }
            .drawWithCache {
                val w = size.width
                val h = size.height

                val halfW = w / 2f
                val halfH = h / 2f
                val radius = size.minDimension / 2f

                val offset = 0f

                val lTriangle = RoundedPolygon(
                    numVertices = 3,
                    radius = radius,
                    centerX = halfW - offset,
                    centerY = halfH,
                    rounding = CornerRounding(radius / 5)
                )
                val rTriangle = RoundedPolygon(
                    numVertices = 3,
                    radius = radius,
                    centerX = halfW - offset,
                    centerY = halfH,
                    rounding = CornerRounding(radius / 5)
                )

                val spacerHalfWidth = 4f
                val borderSpacerWidth = 5f
                val borderSpacerHeight = 5f
                val lSquare = RoundedPolygon(
                    vertices = floatArrayOf(
                        borderSpacerWidth, borderSpacerHeight,
                        halfW - spacerHalfWidth, borderSpacerHeight,
                        halfW - spacerHalfWidth, h - borderSpacerHeight,
                        borderSpacerWidth, h - borderSpacerHeight
                    ),
                    rounding = CornerRounding(radius / 5)
                )
                val rSquare = RoundedPolygon(
                    vertices = floatArrayOf(
                        halfW + spacerHalfWidth, borderSpacerHeight,
                        w - borderSpacerWidth, borderSpacerHeight,
                        w - borderSpacerWidth, h - borderSpacerHeight,
                        halfW + spacerHalfWidth, h - borderSpacerHeight
                    ),
                    rounding = CornerRounding(radius / 5)
                )

                val lMorph = Morph(start = lTriangle, end = lSquare).toPath(animateProgress)
                val rMorph = Morph(start = rTriangle, end = rSquare).toPath(animateProgress)

                onDrawBehind {
                    drawPath(lMorph, color = fill)
                    drawPath(rMorph, color = fill)
                }
            }
    )
}