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
public val ic_plumbing: ImageVector
  get() {
    if (_plumbing != null) {
      return _plumbing!!
    }
    _plumbing =
      ImageVector.Builder(
          name = "plumbing",
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
            moveTo(19.28f, 9.17f)
            lineTo(15.75f, 5.65f)
            lineToRelative(-2.13f, 2.1f)
            lineTo(11.5f, 5.65f)
            lineTo(14.33f, 2.8f)
            quadTo(14.63f, 2.5f, 15f, 2.36f)
            reflectiveQuadTo(15.75f, 2.22f)
            quadToRelative(0.4f, 0f, 0.76f, 0.14f)
            quadTo(16.88f, 2.5f, 17.15f, 2.8f)
            lineToRelative(2.13f, 2.13f)
            quadToRelative(0.45f, 0.42f, 0.66f, 0.99f)
            reflectiveQuadToRelative(0.21f, 1.14f)
            reflectiveQuadTo(19.94f, 8.17f)
            quadToRelative(-0.21f, 0.55f, -0.66f, 1f)
            close()
            moveTo(5.5f, 13.77f)
            quadTo(5.05f, 13.33f, 5.05f, 12.71f)
            reflectiveQuadTo(5.5f, 11.65f)
            lineTo(7.95f, 9.17f)
            lineToRelative(2.13f, 2.13f)
            lineTo(7.6f, 13.77f)
            quadTo(7.18f, 14.23f, 6.56f, 14.23f)
            reflectiveQuadTo(5.5f, 13.77f)
            close()
            moveTo(4.43f, 21.2f)
            quadTo(4.15f, 20.9f, 4f, 20.54f)
            reflectiveQuadTo(3.85f, 19.77f)
            quadToRelative(0f, -0.4f, 0.14f, -0.76f)
            reflectiveQuadTo(4.43f, 18.35f)
            lineTo(11.5f, 11.3f)
            lineTo(8.33f, 8.1f)
            quadTo(7.88f, 7.68f, 7.88f, 7.06f)
            reflectiveQuadTo(8.33f, 6f)
            quadTo(8.75f, 5.55f, 9.38f, 5.55f)
            reflectiveQuadTo(10.45f, 6f)
            lineToRelative(3.17f, 3.17f)
            lineTo(15.05f, 7.75f)
            lineToRelative(2.8f, 2.85f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
            reflectiveQuadTo(17.85f, 12f)
            reflectiveQuadToRelative(-0.7f, 0.3f)
            reflectiveQuadTo(16.45f, 12f)
            lineToRelative(-9.2f, 9.2f)
            quadToRelative(-0.3f, 0.3f, -0.66f, 0.44f)
            reflectiveQuadTo(5.85f, 21.78f)
            reflectiveQuadTo(5.1f, 21.63f)
            reflectiveQuadTo(4.43f, 21.2f)
            close()
          }
        }
        .build()
    return _plumbing!!
  }

private var _plumbing: ImageVector? = null
