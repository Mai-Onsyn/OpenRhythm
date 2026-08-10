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
public val ic_ios_share: ImageVector
  get() {
    if (_ios_share != null) {
      return _ios_share!!
    }
    _ios_share =
      ImageVector.Builder(
          name = "ios_share",
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
            moveTo(6f, 22f)
            quadTo(5.18f, 22f, 4.59f, 21.41f)
            reflectiveQuadTo(4f, 20f)
            verticalLineTo(10f)
            quadTo(4f, 9.17f, 4.59f, 8.59f)
            reflectiveQuadTo(6f, 8f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(6f)
            verticalLineTo(20f)
            horizontalLineTo(18f)
            verticalLineTo(10f)
            horizontalLineTo(15f)
            verticalLineTo(8f)
            horizontalLineToRelative(3f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(20f, 10f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 22f)
            horizontalLineTo(6f)
            close()
            moveToRelative(5f, -6f)
            verticalLineTo(4.82f)
            lineTo(9.4f, 6.43f)
            lineTo(8f, 5f)
            lineTo(12f, 1f)
            lineToRelative(4f, 4f)
            lineTo(14.6f, 6.43f)
            lineTo(13f, 4.82f)
            verticalLineTo(16f)
            horizontalLineTo(11f)
            close()
          }
        }
        .build()
    return _ios_share!!
  }

private var _ios_share: ImageVector? = null
