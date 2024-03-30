@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package dev.jonpoulton.actual.core.icons

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

val ActualIcons.Key: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = Builder(
      name = "Key",
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
        moveTo(23.113f, 5.954f)
        lineTo(21.849f, 4.69f)
        lineToRelative(0.881f, -0.88f)
        arcTo(1.914f, 1.914f, 0.0f, true, false, 20.023f, 1.1f)
        lineTo(9.04f, 12.085f)
        arcToRelative(6.014f, 6.014f, 0.0f, true, false, 2.707f, 2.707f)
        lineToRelative(3.739f, -3.737f)
        lineToRelative(0.762f, 0.762f)
        arcToRelative(1.75f, 1.75f, 0.0f, true, false, 2.475f, -2.475f)
        lineTo(17.96f, 8.58f)
        lineToRelative(1.416f, -1.417f)
        lineTo(20.64f, 8.426f)
        arcToRelative(1.788f, 1.788f, 0.0f, false, false, 2.473f, 0.0f)
        arcTo(1.751f, 1.751f, 0.0f, false, false, 23.113f, 5.954f)
        close()
        moveTo(6.376f, 14.454f)
        arcToRelative(3.0f, 3.0f, 0.0f, true, true, -3.0f, 3.0f)
        arcTo(3.0f, 3.0f, 0.0f, false, true, 6.376f, 14.456f)
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
    imageVector = ActualIcons.Key,
    contentDescription = null,
  )
}
