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
public val ic_dark_mode: ImageVector
  get() {
    if (_dark_mode != null) {
      return _dark_mode!!
    }
    _dark_mode =
      ImageVector.Builder(
          name = "dark_mode",
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
            moveTo(12f, 21f)
            quadTo(8.25f, 21f, 5.63f, 18.38f)
            reflectiveQuadTo(3f, 12f)
            reflectiveQuadTo(5.63f, 5.63f)
            reflectiveQuadTo(12f, 3f)
            quadToRelative(0.35f, 0f, 0.69f, 0.02f)
            quadToRelative(0.34f, 0.02f, 0.66f, 0.07f)
            quadTo(12.33f, 3.82f, 11.71f, 4.99f)
            reflectiveQuadTo(11.1f, 7.5f)
            quadToRelative(0f, 2.25f, 1.57f, 3.82f)
            reflectiveQuadTo(16.5f, 12.9f)
            quadToRelative(1.38f, 0f, 2.53f, -0.61f)
            reflectiveQuadTo(20.9f, 10.65f)
            quadToRelative(0.05f, 0.33f, 0.07f, 0.66f)
            reflectiveQuadTo(21f, 12f)
            quadToRelative(0f, 3.75f, -2.63f, 6.38f)
            reflectiveQuadTo(12f, 21f)
            close()
            moveToRelative(0f, -2f)
            quadToRelative(2.2f, 0f, 3.95f, -1.21f)
            reflectiveQuadTo(18.5f, 14.63f)
            quadToRelative(-0.5f, 0.13f, -1f, 0.2f)
            reflectiveQuadToRelative(-1f, 0.08f)
            quadToRelative(-3.07f, 0f, -5.24f, -2.16f)
            quadTo(9.1f, 10.58f, 9.1f, 7.5f)
            quadTo(9.1f, 7f, 9.18f, 6.5f)
            reflectiveQuadToRelative(0.2f, -1f)
            quadTo(7.43f, 6.3f, 6.21f, 8.05f)
            reflectiveQuadTo(5f, 12f)
            quadToRelative(0f, 2.9f, 2.05f, 4.95f)
            reflectiveQuadTo(12f, 19f)
            close()
            moveTo(11.75f, 12.25f)
            close()
          }
        }
        .build()
    return _dark_mode!!
  }

private var _dark_mode: ImageVector? = null
