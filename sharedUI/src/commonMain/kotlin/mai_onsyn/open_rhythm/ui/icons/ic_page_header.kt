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
public val ic_page_header: ImageVector
  get() {
    if (_page_header != null) {
      return _page_header!!
    }
    _page_header =
      ImageVector.Builder(
          name = "page_header",
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
            moveTo(3f, 5f)
            verticalLineTo(3f)
            horizontalLineTo(21f)
            verticalLineTo(5f)
            horizontalLineTo(3f)
            close()
            moveTo(19f, 7f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 9f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            horizontalLineTo(5f)
            quadTo(4.18f, 21f, 3.59f, 20.41f)
            reflectiveQuadTo(3f, 19f)
            verticalLineTo(9f)
            quadTo(3f, 8.17f, 3.59f, 7.59f)
            reflectiveQuadTo(5f, 7f)
            horizontalLineTo(19f)
            close()
            moveToRelative(0f, 2f)
            horizontalLineTo(5f)
            verticalLineTo(19f)
            horizontalLineTo(19f)
            verticalLineTo(9f)
            close()
            moveTo(5f, 9f)
            verticalLineTo(19f)
            verticalLineTo(9f)
            close()
          }
        }
        .build()
    return _page_header!!
  }

private var _page_header: ImageVector? = null
