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
public val ic_sort: ImageVector
  get() {
    if (_sort != null) {
      return _sort!!
    }
    _sort =
      ImageVector.Builder(
          name = "sort",
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
            moveTo(3f, 18f)
            verticalLineTo(16f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(3f)
            close()
            moveTo(3f, 13f)
            verticalLineTo(11f)
            horizontalLineTo(15f)
            verticalLineToRelative(2f)
            horizontalLineTo(3f)
            close()
            moveTo(3f, 8f)
            verticalLineTo(6f)
            horizontalLineTo(21f)
            verticalLineTo(8f)
            horizontalLineTo(3f)
            close()
          }
        }
        .build()
    return _sort!!
  }

private var _sort: ImageVector? = null
