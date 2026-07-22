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
public val ic_waterfall_chart: ImageVector
  get() {
    if (_waterfall_chart != null) {
      return _waterfall_chart!!
    }
    _waterfall_chart =
      ImageVector.Builder(
          name = "waterfall_chart",
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
            moveTo(3f, 20f)
            verticalLineTo(14f)
            horizontalLineTo(6f)
            verticalLineToRelative(6f)
            horizontalLineTo(3f)
            close()
            moveTo(8f, 14f)
            verticalLineTo(9f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(5f)
            horizontalLineTo(8f)
            close()
            moveTo(13f, 9f)
            verticalLineTo(4f)
            horizontalLineToRelative(3f)
            verticalLineTo(9f)
            horizontalLineTo(13f)
            close()
            moveToRelative(5f, 11f)
            verticalLineTo(4f)
            horizontalLineToRelative(3f)
            verticalLineTo(20f)
            horizontalLineTo(18f)
            close()
          }
        }
        .build()
    return _waterfall_chart!!
  }

private var _waterfall_chart: ImageVector? = null
