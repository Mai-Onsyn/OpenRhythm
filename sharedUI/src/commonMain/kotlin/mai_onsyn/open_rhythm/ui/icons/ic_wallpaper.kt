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
public val ic_wallpaper: ImageVector
  get() {
    if (_wallpaper != null) {
      return _wallpaper!!
    }
    _wallpaper =
      ImageVector.Builder(
          name = "wallpaper",
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
            moveTo(5f, 21f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(13f)
            horizontalLineTo(5f)
            verticalLineToRelative(6f)
            horizontalLineToRelative(6f)
            verticalLineToRelative(2f)
            horizontalLineTo(5f)
            close()
            moveToRelative(8f, 0f)
            verticalLineTo(19f)
            horizontalLineToRelative(6f)
            verticalLineTo(13f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(13f)
            close()
            moveTo(6f, 17f)
            lineTo(9f, 13f)
            lineToRelative(2.25f, 3f)
            lineToRelative(3f, -4f)
            lineTo(18f, 17f)
            horizontalLineTo(6f)
            close()
            moveTo(3f, 11f)
            verticalLineTo(5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineToRelative(6f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineToRelative(6f)
            horizontalLineTo(3f)
            close()
            moveToRelative(16f, 0f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(3f)
            horizontalLineToRelative(6f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineToRelative(6f)
            horizontalLineTo(19f)
            close()
            moveTo(14.43f, 9.57f)
            quadTo(14f, 9.15f, 14f, 8.5f)
            reflectiveQuadTo(14.43f, 7.43f)
            reflectiveQuadTo(15.5f, 7f)
            reflectiveQuadToRelative(1.07f, 0.43f)
            reflectiveQuadTo(17f, 8.5f)
            reflectiveQuadTo(16.58f, 9.57f)
            reflectiveQuadTo(15.5f, 10f)
            reflectiveQuadTo(14.43f, 9.57f)
            close()
          }
        }
        .build()
    return _wallpaper!!
  }

private var _wallpaper: ImageVector? = null
