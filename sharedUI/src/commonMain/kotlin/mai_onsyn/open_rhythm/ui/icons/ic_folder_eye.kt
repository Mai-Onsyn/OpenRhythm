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
public val ic_folder_eye: ImageVector
  get() {
    if (_folder_eye != null) {
      return _folder_eye!!
    }
    _folder_eye =
      ImageVector.Builder(
          name = "folder_eye",
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
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineToRelative(6f)
            lineToRelative(2f, 2f)
            horizontalLineToRelative(8f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 7.18f, 22f, 8f)
            verticalLineToRelative(6.05f)
            quadTo(21.55f, 13.7f, 21.05f, 13.48f)
            reflectiveQuadTo(20f, 13f)
            verticalLineTo(8f)
            horizontalLineTo(11.18f)
            lineToRelative(-2f, -2f)
            horizontalLineTo(4f)
            verticalLineTo(18f)
            horizontalLineTo(7f)
            quadToRelative(0f, 0.45f, 0f, 1f)
            reflectiveQuadToRelative(0f, 1f)
            horizontalLineTo(4f)
            close()
            moveToRelative(12f, 3f)
            quadToRelative(-2.27f, 0f, -4.2f, -1.2f)
            reflectiveQuadTo(9f, 18.5f)
            quadToRelative(0.88f, -2.1f, 2.8f, -3.3f)
            reflectiveQuadTo(16f, 14f)
            quadToRelative(2.28f, 0f, 4.2f, 1.2f)
            reflectiveQuadTo(23f, 18.5f)
            quadToRelative(-0.88f, 2.1f, -2.8f, 3.3f)
            reflectiveQuadTo(16f, 23f)
            close()
            moveToRelative(2.69f, -2.65f)
            quadTo(19.95f, 19.7f, 20.75f, 18.5f)
            quadToRelative(-0.8f, -1.2f, -2.06f, -1.85f)
            quadTo(17.43f, 16f, 16f, 16f)
            reflectiveQuadToRelative(-2.69f, 0.65f)
            reflectiveQuadTo(11.25f, 18.5f)
            quadToRelative(0.8f, 1.2f, 2.06f, 1.85f)
            reflectiveQuadTo(16f, 21f)
            quadToRelative(1.43f, 0f, 2.69f, -0.65f)
            close()
            moveTo(14.94f, 19.56f)
            quadTo(14.5f, 19.13f, 14.5f, 18.5f)
            reflectiveQuadToRelative(0.44f, -1.06f)
            reflectiveQuadTo(16f, 17f)
            reflectiveQuadToRelative(1.06f, 0.44f)
            reflectiveQuadTo(17.5f, 18.5f)
            reflectiveQuadToRelative(-0.44f, 1.06f)
            reflectiveQuadTo(16f, 20f)
            reflectiveQuadTo(14.94f, 19.56f)
            close()
            moveTo(4f, 18f)
            verticalLineTo(6f)
            verticalLineTo(8f)
            verticalLineToRelative(4.92f)
            quadTo(4f, 12.48f, 4f, 12.24f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(0f, 1.6f)
            reflectiveQuadTo(4f, 18f)
            close()
          }
        }
        .build()
    return _folder_eye!!
  }

private var _folder_eye: ImageVector? = null
