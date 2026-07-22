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
public val ic_keyboard: ImageVector
  get() {
    if (_keyboard_alt != null) {
      return _keyboard_alt!!
    }
    _keyboard_alt =
      ImageVector.Builder(
          name = "keyboard_alt",
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
            quadTo(2.18f, 20f, 1.59f, 19.41f)
            reflectiveQuadTo(1f, 18f)
            verticalLineTo(6f)
            quadTo(1f, 5.18f, 1.59f, 4.59f)
            reflectiveQuadTo(3f, 4f)
            horizontalLineTo(21f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(23f, 5.18f, 23f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(21f, 20f)
            horizontalLineTo(3f)
            close()
            moveTo(3f, 18f)
            horizontalLineTo(21f)
            verticalLineTo(6f)
            horizontalLineTo(3f)
            verticalLineTo(18f)
            close()
            moveTo(8f, 17f)
            horizontalLineToRelative(8f)
            verticalLineTo(15f)
            horizontalLineTo(8f)
            verticalLineToRelative(2f)
            close()
            moveTo(5f, 13.5f)
            horizontalLineTo(7f)
            verticalLineToRelative(-2f)
            horizontalLineTo(5f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-2f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-2f)
            horizontalLineTo(13f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(-2f)
            horizontalLineTo(17f)
            verticalLineToRelative(2f)
            close()
            moveTo(5f, 10f)
            horizontalLineTo(7f)
            verticalLineTo(8f)
            horizontalLineTo(5f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(13f)
            verticalLineToRelative(2f)
            close()
            moveToRelative(4f, 0f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(17f)
            verticalLineToRelative(2f)
            close()
            moveTo(3f, 18f)
            verticalLineTo(6f)
            verticalLineTo(18f)
            close()
          }
        }
        .build()
    return _keyboard_alt!!
  }

private var _keyboard_alt: ImageVector? = null
