package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import mai_onsyn.open_rhythm.bridge.Global

fun DrawScope.drawNoteGraphics(
    color: Color,
    rect: Rect,
    cornerRadius: Float,
) {
    // shadow
    if (Global.settings.DrawNoteShadow) drawRoundRect(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = rect.topLeft + Offset(rect.width * 0.1f, rect.width * 0.07f),
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius)
    )

    // base
    drawRoundRect(
        color = color,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius)
    )
}