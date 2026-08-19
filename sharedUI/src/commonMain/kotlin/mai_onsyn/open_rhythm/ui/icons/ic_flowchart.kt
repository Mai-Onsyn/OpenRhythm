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
public val ic_flowchart: ImageVector
  get() {
    if (_flowchart != null) {
      return _flowchart!!
    }
    _flowchart =
      ImageVector.Builder(
          name = "flowchart",
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
            moveTo(15f, 20f)
            verticalLineTo(18f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineTo(2f)
            verticalLineTo(9f)
            horizontalLineTo(9f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(2f)
            verticalLineTo(6f)
            horizontalLineToRelative(4f)
            verticalLineTo(4f)
            horizontalLineToRelative(7f)
            verticalLineToRelative(6f)
            horizontalLineTo(15f)
            verticalLineTo(8f)
            horizontalLineTo(13f)
            verticalLineToRelative(8f)
            horizontalLineToRelative(2f)
            verticalLineTo(14f)
            horizontalLineToRelative(7f)
            verticalLineToRelative(6f)
            horizontalLineTo(15f)
            close()
            moveToRelative(2f, -2f)
            horizontalLineToRelative(3f)
            verticalLineTo(16f)
            horizontalLineTo(17f)
            verticalLineToRelative(2f)
            close()
            moveTo(4f, 13f)
            horizontalLineTo(7f)
            verticalLineTo(11f)
            horizontalLineTo(4f)
            verticalLineToRelative(2f)
            close()
            moveTo(17f, 8f)
            horizontalLineToRelative(3f)
            verticalLineTo(6f)
            horizontalLineTo(17f)
            verticalLineTo(8f)
            close()
            moveToRelative(0f, 10f)
            verticalLineTo(16f)
            verticalLineToRelative(2f)
            close()
            moveTo(7f, 13f)
            verticalLineTo(11f)
            verticalLineToRelative(2f)
            close()
            moveTo(17f, 8f)
            verticalLineTo(6f)
            verticalLineTo(8f)
            close()
          }
        }
        .build()
    return _flowchart!!
  }

private var _flowchart: ImageVector? = null
