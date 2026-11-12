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
public val ic_flash_on: ImageVector
  get() {
    if (_flash_on != null) {
      return _flash_on!!
    }
    _flash_on =
      ImageVector.Builder(
          name = "flash_on",
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
            moveTo(12f, 15.6f)
            lineTo(15.2f, 11f)
            horizontalLineTo(12.35f)
            lineToRelative(2f, -7f)
            horizontalLineTo(9f)
            verticalLineToRelative(8f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(3.6f)
            close()
            moveTo(10f, 22f)
            verticalLineTo(14f)
            horizontalLineTo(7f)
            verticalLineTo(2f)
            horizontalLineTo(17f)
            lineTo(15f, 9f)
            horizontalLineToRelative(4f)
            lineTo(10f, 22f)
            close()
            moveTo(12f, 12f)
            horizontalLineTo(9f)
            horizontalLineToRelative(3f)
            close()
          }
        }
        .build()
    return _flash_on!!
  }

private var _flash_on: ImageVector? = null
