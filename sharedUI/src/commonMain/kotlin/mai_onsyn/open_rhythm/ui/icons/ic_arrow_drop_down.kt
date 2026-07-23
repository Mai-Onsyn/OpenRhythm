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
public val ic_arrow_drop_down: ImageVector
  get() {
    if (_arrow_drop_down != null) {
      return _arrow_drop_down!!
    }
    _arrow_drop_down =
      ImageVector.Builder(
          name = "arrow_drop_down",
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
            moveTo(12f, 15f)
            lineTo(7f, 10f)
            horizontalLineTo(17f)
            lineToRelative(-5f, 5f)
            close()
          }
        }
        .build()
    return _arrow_drop_down!!
  }

private var _arrow_drop_down: ImageVector? = null
