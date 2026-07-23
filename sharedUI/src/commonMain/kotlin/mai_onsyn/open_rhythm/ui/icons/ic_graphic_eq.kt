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
public val ic_graphic_eq: ImageVector
  get() {
    if (_graphic_eq != null) {
      return _graphic_eq!!
    }
    _graphic_eq =
      ImageVector.Builder(
          name = "graphic_eq",
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
            moveTo(7f, 18f)
            verticalLineTo(6f)
            horizontalLineTo(9f)
            verticalLineTo(18f)
            horizontalLineTo(7f)
            close()
            moveToRelative(4f, 4f)
            verticalLineTo(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(22f)
            horizontalLineTo(11f)
            close()
            moveTo(3f, 14f)
            verticalLineTo(10f)
            horizontalLineTo(5f)
            verticalLineToRelative(4f)
            horizontalLineTo(3f)
            close()
            moveToRelative(12f, 4f)
            verticalLineTo(6f)
            horizontalLineToRelative(2f)
            verticalLineTo(18f)
            horizontalLineTo(15f)
            close()
            moveToRelative(4f, -4f)
            verticalLineTo(10f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(4f)
            horizontalLineTo(19f)
            close()
          }
        }
        .build()
    return _graphic_eq!!
  }

private var _graphic_eq: ImageVector? = null
