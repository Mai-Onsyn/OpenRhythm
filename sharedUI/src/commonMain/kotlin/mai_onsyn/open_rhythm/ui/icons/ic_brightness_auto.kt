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
public val ic_brightness_auto: ImageVector
  get() {
    if (_brightness_auto != null) {
      return _brightness_auto!!
    }
    _brightness_auto =
      ImageVector.Builder(
          name = "brightness_auto",
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
            moveTo(7.8f, 16f)
            horizontalLineTo(9.4f)
            lineToRelative(0.8f, -2.3f)
            horizontalLineToRelative(3.65f)
            lineToRelative(0.8f, 2.3f)
            horizontalLineTo(16.2f)
            lineTo(12.8f, 7f)
            horizontalLineTo(11.2f)
            lineTo(7.8f, 16f)
            close()
            moveToRelative(2.85f, -3.6f)
            lineToRelative(1.3f, -3.75f)
            horizontalLineToRelative(0.1f)
            lineToRelative(1.3f, 3.75f)
            horizontalLineToRelative(-2.7f)
            close()
            moveTo(12f, 23.3f)
            lineTo(8.65f, 20f)
            horizontalLineTo(4f)
            verticalLineTo(15.35f)
            lineTo(0.7f, 12f)
            lineTo(4f, 8.65f)
            verticalLineTo(4f)
            horizontalLineTo(8.65f)
            lineTo(12f, 0.7f)
            lineTo(15.35f, 4f)
            horizontalLineTo(20f)
            verticalLineTo(8.65f)
            lineTo(23.3f, 12f)
            lineTo(20f, 15.35f)
            verticalLineTo(20f)
            horizontalLineTo(15.35f)
            lineTo(12f, 23.3f)
            close()
            moveToRelative(0f, -2.8f)
            lineTo(14.5f, 18f)
            horizontalLineTo(18f)
            verticalLineTo(14.5f)
            lineTo(20.5f, 12f)
            lineTo(18f, 9.5f)
            verticalLineTo(6f)
            horizontalLineTo(14.5f)
            lineTo(12f, 3.5f)
            lineTo(9.5f, 6f)
            horizontalLineTo(6f)
            verticalLineTo(9.5f)
            lineTo(3.5f, 12f)
            lineTo(6f, 14.5f)
            verticalLineTo(18f)
            horizontalLineTo(9.5f)
            lineTo(12f, 20.5f)
            close()
            moveTo(12f, 12f)
            close()
          }
        }
        .build()
    return _brightness_auto!!
  }

private var _brightness_auto: ImageVector? = null
