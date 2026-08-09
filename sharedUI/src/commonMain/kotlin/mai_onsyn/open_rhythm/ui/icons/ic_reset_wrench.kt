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
public val ic_reset_wrench: ImageVector
  get() {
    if (_reset_wrench != null) {
      return _reset_wrench!!
    }
    _reset_wrench =
      ImageVector.Builder(
          name = "reset_wrench",
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
            moveTo(16.33f, 18.8f)
            lineTo(14.13f, 21f)
            lineTo(12f, 18.88f)
            lineToRelative(2.2f, -2.2f)
            quadTo(14.1f, 16.4f, 14.05f, 16.1f)
            reflectiveQuadTo(14f, 15.5f)
            quadToRelative(0f, -1.45f, 1.03f, -2.48f)
            reflectiveQuadTo(17.5f, 12f)
            quadToRelative(0.45f, 0f, 0.88f, 0.11f)
            quadToRelative(0.43f, 0.11f, 0.8f, 0.31f)
            lineTo(16.8f, 14.8f)
            lineToRelative(1.4f, 1.4f)
            lineToRelative(2.38f, -2.35f)
            quadToRelative(0.2f, 0.38f, 0.31f, 0.79f)
            reflectiveQuadTo(21f, 15.5f)
            quadToRelative(0f, 1.45f, -1.02f, 2.48f)
            reflectiveQuadTo(17.5f, 19f)
            quadToRelative(-0.32f, 0f, -0.61f, -0.05f)
            reflectiveQuadTo(16.33f, 18.8f)
            close()
            moveTo(20.78f, 10f)
            horizontalLineTo(18.7f)
            quadTo(18.05f, 7.8f, 16.23f, 6.4f)
            reflectiveQuadTo(12f, 5f)
            quadTo(9.08f, 5f, 7.04f, 7.04f)
            reflectiveQuadTo(5f, 12f)
            quadToRelative(0f, 1.8f, 0.81f, 3.3f)
            reflectiveQuadTo(8f, 17.75f)
            verticalLineTo(15f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(6f)
            horizontalLineTo(4f)
            verticalLineTo(19f)
            horizontalLineTo(6.35f)
            quadTo(4.8f, 17.75f, 3.9f, 15.94f)
            reflectiveQuadTo(3f, 12f)
            quadTo(3f, 10.13f, 3.71f, 8.49f)
            reflectiveQuadTo(5.64f, 5.64f)
            quadTo(6.85f, 4.42f, 8.49f, 3.71f)
            reflectiveQuadTo(12f, 3f)
            quadToRelative(3.23f, 0f, 5.66f, 1.99f)
            quadTo(20.1f, 6.97f, 20.78f, 10f)
            close()
          }
        }
        .build()
    return _reset_wrench!!
  }

private var _reset_wrench: ImageVector? = null
