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
public val ic_instant_mix: ImageVector
  get() {
    if (_instant_mix != null) {
      return _instant_mix!!
    }
    _instant_mix =
      ImageVector.Builder(
          name = "instant_mix",
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
            moveTo(5f, 20f)
            verticalLineTo(13f)
            horizontalLineTo(3f)
            verticalLineTo(11f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(7f)
            verticalLineToRelative(7f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 9f)
            verticalLineTo(4f)
            horizontalLineTo(7f)
            verticalLineTo(9f)
            horizontalLineTo(5f)
            close()
            moveTo(9f, 9f)
            verticalLineTo(7f)
            horizontalLineToRelative(2f)
            verticalLineTo(4f)
            horizontalLineToRelative(2f)
            verticalLineTo(7f)
            horizontalLineToRelative(2f)
            verticalLineTo(9f)
            horizontalLineTo(9f)
            close()
            moveToRelative(2f, 11f)
            verticalLineTo(11f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(9f)
            horizontalLineTo(11f)
            close()
            moveToRelative(6f, 0f)
            verticalLineTo(17f)
            horizontalLineTo(15f)
            verticalLineTo(15f)
            horizontalLineToRelative(6f)
            verticalLineToRelative(2f)
            horizontalLineTo(19f)
            verticalLineToRelative(3f)
            horizontalLineTo(17f)
            close()
            moveToRelative(0f, -7f)
            verticalLineTo(4f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(9f)
            horizontalLineTo(17f)
            close()
          }
        }
        .build()
    return _instant_mix!!
  }

private var _instant_mix: ImageVector? = null
