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
public val ic_unknown: ImageVector
  get() {
    if (_indeterminate_question_box != null) {
      return _indeterminate_question_box!!
    }
    _indeterminate_question_box =
      ImageVector.Builder(
          name = "indeterminate_question_box",
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
            verticalLineTo(15f)
            horizontalLineTo(5f)
            verticalLineToRelative(4f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(5f)
            close()
            moveToRelative(14f, 0f)
            horizontalLineTo(15f)
            verticalLineTo(19f)
            horizontalLineToRelative(4f)
            verticalLineTo(15f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 21f)
            close()
            moveTo(3f, 5f)
            quadTo(3f, 4.17f, 3.59f, 3.59f)
            reflectiveQuadTo(5f, 3f)
            horizontalLineTo(9f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(9f)
            horizontalLineTo(3f)
            verticalLineTo(5f)
            close()
            moveTo(21f, 5f)
            verticalLineTo(9f)
            horizontalLineTo(19f)
            verticalLineTo(5f)
            horizontalLineTo(15f)
            verticalLineTo(3f)
            horizontalLineToRelative(4f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(21f, 5f)
            close()
            moveTo(12.89f, 17.64f)
            quadToRelative(0.36f, -0.36f, 0.36f, -0.89f)
            reflectiveQuadTo(12.89f, 15.86f)
            reflectiveQuadTo(12f, 15.5f)
            reflectiveQuadToRelative(-0.89f, 0.36f)
            quadToRelative(-0.36f, 0.36f, -0.36f, 0.89f)
            reflectiveQuadToRelative(0.36f, 0.89f)
            reflectiveQuadTo(12f, 18f)
            quadToRelative(0.53f, 0f, 0.89f, -0.36f)
            close()
            moveTo(11.1f, 14.18f)
            horizontalLineToRelative(1.82f)
            quadToRelative(0f, -0.85f, 0.2f, -1.3f)
            reflectiveQuadTo(14f, 11.75f)
            quadToRelative(0.88f, -0.88f, 1.16f, -1.41f)
            reflectiveQuadTo(15.45f, 9.05f)
            quadToRelative(0f, -1.35f, -0.97f, -2.2f)
            reflectiveQuadTo(12f, 6f)
            quadTo(10.75f, 6f, 9.85f, 6.65f)
            reflectiveQuadTo(8.55f, 8.5f)
            lineTo(10.2f, 9.17f)
            quadTo(10.38f, 8.52f, 10.86f, 8.11f)
            reflectiveQuadTo(12f, 7.7f)
            quadToRelative(0.73f, 0f, 1.16f, 0.39f)
            quadTo(13.6f, 8.48f, 13.6f, 9.13f)
            quadToRelative(0f, 0.5f, -0.24f, 0.94f)
            reflectiveQuadToRelative(-0.81f, 0.91f)
            quadToRelative(-0.82f, 0.72f, -1.14f, 1.4f)
            reflectiveQuadToRelative(-0.31f, 1.8f)
            close()
          }
        }
        .build()
    return _indeterminate_question_box!!
  }

private var _indeterminate_question_box: ImageVector? = null
