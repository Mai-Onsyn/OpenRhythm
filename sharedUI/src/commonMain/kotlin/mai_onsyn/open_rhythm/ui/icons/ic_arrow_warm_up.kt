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
public val ic_arrow_warm_up: ImageVector
  get() {
    if (_arrow_warm_up != null) {
      return _arrow_warm_up!!
    }
    _arrow_warm_up =
      ImageVector.Builder(
          name = "arrow_warm_up",
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
            moveTo(11f, 5.82f)
            lineTo(6.4f, 10.4f)
            lineTo(5f, 9f)
            lineTo(12f, 2f)
            lineToRelative(7f, 7f)
            lineToRelative(-1.4f, 1.42f)
            lineTo(13f, 5.82f)
            verticalLineTo(13f)
            horizontalLineTo(11f)
            verticalLineTo(5.82f)
            close()
            moveTo(11f, 18f)
            verticalLineTo(15f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(3f)
            horizontalLineTo(11f)
            close()
            moveToRelative(0f, 4f)
            verticalLineTo(20f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineTo(11f)
            close()
          }
        }
        .build()
    return _arrow_warm_up!!
  }

private var _arrow_warm_up: ImageVector? = null
