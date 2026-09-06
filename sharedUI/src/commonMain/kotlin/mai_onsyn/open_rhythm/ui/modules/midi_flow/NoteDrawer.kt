package mai_onsyn.open_rhythm.ui.modules.midi_flow

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import mai_onsyn.open_rhythm.bridge.Global

fun DrawScope.drawNoteGraphics(
    color: Color,
    rect: Rect,
    cornerRadius: Float
) {
    // shadow
    if (Global.settings.DrawNoteShadow) drawRoundRect(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = rect.topLeft + Offset(rect.width * 0.1f, rect.width * 0.07f),
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius)
    )

    val c = if (Global.settings.NoteOpacity < 1f) color.copy(alpha = Global.settings.NoteOpacity) else color
    // base
    drawRoundRect(
        color = c,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius)
    )
}

private val cachedColorPaint = mutableMapOf<Color, Paint>()

fun drawNoteGraphics(
    canvas: Canvas,
    color: Color,
    rect: Rect,
    cornerRadius: Float
) {
    // shadow
    if (Global.settings.DrawNoteShadow) {
        val shadowRect = Rect(rect.topLeft + Offset(rect.width * 0.1f, rect.width * 0.07f), rect.size)
        canvas.drawRoundRect(
            shadowRect.left,
            shadowRect.top,
            shadowRect.right,
            shadowRect.bottom,
            cornerRadius,
            cornerRadius,
            cachedColorPaint.getOrPut(Color.Black.copy(alpha = 0.12f)) {
                Paint().apply {
                    this.color = Color.Black.copy(alpha = 0.12f)
                }
            }
        )
    }

    // base
    val c = if (Global.settings.NoteOpacity < 1f) color.copy(alpha = Global.settings.NoteOpacity) else color
    canvas.drawRoundRect(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
        cornerRadius,
        cornerRadius,
        cachedColorPaint.getOrPut(c) {
            Paint().apply {
                this.color = c
            }
        }
    )
}