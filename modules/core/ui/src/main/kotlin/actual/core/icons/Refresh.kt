@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package actual.core.icons

import actual.core.ui.PreviewActualRow
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

val ActualIcons.Refresh: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = Builder(
      name = "Refresh",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color.Black),
        stroke = null,
        strokeLineWidth = 0.0f,
        strokeLineCap = Butt,
        strokeLineJoin = Miter,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero,
      ) {
        moveTo(10.664f, 20.073f)
        arcToRelative(1.249f, 1.249f, 0.0f, true, false, -0.507f, 2.447f)
        arcTo(10.739f, 10.739f, 0.0f, true, false, 2.4f, 16.1f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, true, -0.084f, 0.3f)
        lineToRelative(-1.0f, 0.726f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 0.377f, 1.787f)
        lineToRelative(3.946f, 0.849f)
        arcToRelative(1.062f, 1.062f, 0.0f, false, false, 0.21f, 0.022f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 0.978f, -0.79f)
        lineToRelative(0.945f, -4.4f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, -1.565f, -1.02f)
        lineToRelative(-1.361f, 0.989f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, true, -0.386f, -0.128f)
        arcToRelative(8.255f, 8.255f, 0.0f, true, true, 6.205f, 5.643f)
        close()
      }
    }.build()
    return icon!!
  }

private var icon: ImageVector? = null

@Preview
@Composable
private fun Preview() = PreviewActualRow {
  Icon(
    imageVector = ActualIcons.Refresh,
    contentDescription = null,
  )
}
