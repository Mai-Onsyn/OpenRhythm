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
public val ic_settings_input_svideo: ImageVector
  get() {
    if (_settings_input_svideo != null) {
      return _settings_input_svideo!!
    }
    _settings_input_svideo =
      ImageVector.Builder(
          name = "settings_input_svideo",
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
            moveTo(8.13f, 21.21f)
            quadTo(6.3f, 20.43f, 4.94f, 19.06f)
            quadTo(3.58f, 17.7f, 2.79f, 15.88f)
            reflectiveQuadTo(2f, 11.99f)
            reflectiveQuadTo(2.79f, 8.11f)
            reflectiveQuadTo(4.94f, 4.94f)
            reflectiveQuadTo(8.13f, 2.79f)
            reflectiveQuadTo(12.01f, 2f)
            reflectiveQuadToRelative(3.88f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.15f)
            reflectiveQuadToRelative(2.15f, 3.17f)
            reflectiveQuadTo(22f, 11.99f)
            reflectiveQuadToRelative(-0.79f, 3.89f)
            reflectiveQuadToRelative(-2.15f, 3.19f)
            reflectiveQuadToRelative(-3.17f, 2.15f)
            reflectiveQuadTo(12.01f, 22f)
            reflectiveQuadTo(8.13f, 21.21f)
            close()
            moveTo(12f, 12f)
            close()
            moveTo(8.56f, 12.56f)
            quadTo(9f, 12.13f, 9f, 11.5f)
            reflectiveQuadTo(8.56f, 10.44f)
            reflectiveQuadTo(7.5f, 10f)
            reflectiveQuadTo(6.44f, 10.44f)
            reflectiveQuadTo(6f, 11.5f)
            reflectiveQuadToRelative(0.44f, 1.06f)
            reflectiveQuadTo(7.5f, 13f)
            reflectiveQuadTo(8.56f, 12.56f)
            close()
            moveToRelative(9f, 0f)
            quadTo(18f, 12.13f, 18f, 11.5f)
            reflectiveQuadTo(17.56f, 10.44f)
            reflectiveQuadTo(16.5f, 10f)
            reflectiveQuadToRelative(-1.06f, 0.44f)
            reflectiveQuadTo(15f, 11.5f)
            reflectiveQuadToRelative(0.44f, 1.06f)
            reflectiveQuadTo(16.5f, 13f)
            reflectiveQuadToRelative(1.06f, -0.44f)
            close()
            moveToRelative(-7.5f, 4.5f)
            quadTo(10.5f, 16.63f, 10.5f, 16f)
            reflectiveQuadTo(10.06f, 14.94f)
            reflectiveQuadTo(9f, 14.5f)
            reflectiveQuadTo(7.94f, 14.94f)
            reflectiveQuadTo(7.5f, 16f)
            reflectiveQuadToRelative(0.44f, 1.06f)
            reflectiveQuadTo(9f, 17.5f)
            reflectiveQuadToRelative(1.06f, -0.44f)
            close()
            moveToRelative(6f, 0f)
            quadTo(16.5f, 16.63f, 16.5f, 16f)
            reflectiveQuadTo(16.06f, 14.94f)
            reflectiveQuadTo(15f, 14.5f)
            reflectiveQuadToRelative(-1.06f, 0.44f)
            reflectiveQuadTo(13.5f, 16f)
            reflectiveQuadToRelative(0.44f, 1.06f)
            reflectiveQuadTo(15f, 17.5f)
            reflectiveQuadToRelative(1.06f, -0.44f)
            close()
            moveTo(10.5f, 9f)
            horizontalLineToRelative(3f)
            quadToRelative(0.65f, 0f, 1.08f, -0.43f)
            reflectiveQuadTo(15f, 7.5f)
            reflectiveQuadTo(14.58f, 6.43f)
            reflectiveQuadTo(13.5f, 6f)
            horizontalLineToRelative(-3f)
            quadTo(9.85f, 6f, 9.43f, 6.43f)
            reflectiveQuadTo(9f, 7.5f)
            reflectiveQuadTo(9.43f, 8.57f)
            reflectiveQuadTo(10.5f, 9f)
            close()
            moveTo(12f, 20f)
            quadToRelative(3.33f, 0f, 5.66f, -2.34f)
            reflectiveQuadTo(20f, 12f)
            quadTo(20f, 8.67f, 17.66f, 6.34f)
            reflectiveQuadTo(12f, 4f)
            quadTo(8.68f, 4f, 6.34f, 6.34f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(2.34f, 5.66f)
            reflectiveQuadTo(12f, 20f)
            close()
          }
        }
        .build()
    return _settings_input_svideo!!
  }

private var _settings_input_svideo: ImageVector? = null
