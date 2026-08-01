package mai_onsyn.open_rhythm.ui.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit

fun DrawScope.drawTextCentered(
    layoutResult: TextLayoutResult,
    center: Offset
) {
    val width = layoutResult.size.width
    val height = layoutResult.size.height
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(center.x - width / 2, center.y - height / 2)
    )
}

@Composable
fun rememberTextLayoutResult(
    text: String,
    fontSize: TextUnit,
    color: Color
): TextLayoutResult {
    val textMeasurer = rememberTextMeasurer()
    return remember(text, fontSize, color) {
        textMeasurer.measure(
            text = text,
            style = TextStyle(fontSize = fontSize, color = color)
        )
    }
}