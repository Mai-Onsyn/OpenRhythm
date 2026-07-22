package mai_onsyn.open_rhythm.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val ic_arrow_selector_tool: ImageVector
  get() {
    if (_arrow_selector_tool != null) {
      return _arrow_selector_tool!!
    }
    _arrow_selector_tool =
      ImageVector.Builder(
          name = "arrow_selector_tool",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(8f, 13.75f)
            lineTo(9.98f, 11f)
            horizontalLineToRelative(4.25f)
            lineTo(8f, 6.1f)
            verticalLineToRelative(7.65f)
            close()
            moveTo(13.78f, 22f)
            lineTo(10.15f, 14.2f)
            lineTo(6f, 20f)
            verticalLineTo(2f)
            lineTo(20f, 13f)
            horizontalLineTo(12.9f)
            lineToRelative(3.6f, 7.73f)
            lineTo(13.78f, 22f)
            close()
            moveTo(9.98f, 11f)
            close()
          }
        }
        .build()
    return _arrow_selector_tool!!
  }

private var _arrow_selector_tool: ImageVector? = null
