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
public val ic_piano: ImageVector
  get() {
    if (_piano != null) {
      return _piano!!
    }
    _piano =
      ImageVector.Builder(
          name = "piano",
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
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(5f, 21f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 19f)
            horizontalLineTo(8.25f)
            verticalLineTo(14.5f)
            horizontalLineTo(8f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(7f, 13.93f, 7f, 13.5f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            close()
            moveToRelative(10.75f, 0f)
            horizontalLineTo(19f)
            verticalLineTo(5f)
            horizontalLineTo(17f)
            verticalLineToRelative(8.5f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(16f, 14.5f)
            horizontalLineTo(15.75f)
            verticalLineTo(19f)
            close()
            moveToRelative(-6f, 0f)
            horizontalLineToRelative(4.5f)
            verticalLineTo(14.5f)
            horizontalLineTo(14f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(13f, 13.93f, 13f, 13.5f)
            verticalLineTo(5f)
            horizontalLineTo(11f)
            verticalLineToRelative(8.5f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(10f, 14.5f)
            horizontalLineTo(9.75f)
            verticalLineTo(19f)
            close()
          }
        }
        .build()
    return _piano!!
  }

private var _piano: ImageVector? = null
