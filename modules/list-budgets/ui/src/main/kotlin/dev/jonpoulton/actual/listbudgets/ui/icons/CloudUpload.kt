@file:Suppress("BooleanLiteralArgument")

package dev.jonpoulton.actual.listbudgets.ui.icons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.PreviewActualRow

internal val CloudUpload: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = Builder(
      name = "CloudUpload", defaultWidth = 20.0.dp,
      defaultHeight =
      20.0.dp,
      viewportWidth = 20.0f, viewportHeight = 20.0f,
    ).apply {
      path(
        fill = SolidColor(Color.Black), stroke = null, strokeLineWidth = 0.0f,
        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
        pathFillType = NonZero,
      ) {
        moveTo(16.88f, 9.1f)
        arcTo(4.0f, 4.0f, 0.0f, false, true, 16.0f, 17.0f)
        horizontalLineTo(5.0f)
        arcToRelative(5.0f, 5.0f, 0.0f, false, true, -1.0f, -9.9f)
        verticalLineTo(7.0f)
        arcToRelative(3.0f, 3.0f, 0.0f, false, true, 4.52f, -2.59f)
        arcTo(4.98f, 4.98f, 0.0f, false, true, 17.0f, 8.0f)
        curveToRelative(0.0f, 0.38f, -0.04f, 0.74f, -0.12f, 1.1f)
        close()
        moveTo(11.0f, 11.0f)
        horizontalLineToRelative(3.0f)
        lineToRelative(-4.0f, -4.0f)
        lineToRelative(-4.0f, 4.0f)
        horizontalLineToRelative(3.0f)
        verticalLineToRelative(3.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(-3.0f)
        close()
      }
    }
      .build()
    return icon!!
  }

private var icon: ImageVector? = null

@Preview
@Composable
private fun Preview() = PreviewActualRow {
  Icon(
    imageVector = CloudUpload,
    contentDescription = null,
  )
}
