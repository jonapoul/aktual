@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package actual.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ActualIcons.CloseBracket: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = ImageVector
      .Builder(
        name = "CloseBracket",
        defaultWidth = 17.dp,
        defaultHeight = 56.7.dp,
        viewportWidth = 17f,
        viewportHeight = 56.7f,
      ).apply {
        path(fill = SolidColor(Color.Black)) {
          moveTo(1.9f, 2.2f)
          lineTo(1.9f, 2.2f)
          curveToRelative(1.3f, 0.2f, 2.4f, 0.7f, 3.4f, 1.5f)
          curveToRelative(0.8f, 0.8f, 1.5f, 1.8f, 2f, 2.9f)
          curveToRelative(0.9f, 2.3f, 3.1f, 8.4f, 3.1f, 21.8f)
          reflectiveCurveTo(8.2f, 47.8f, 7.3f, 50.1f)
          curveToRelative(-0.5f, 1.1f, -1.2f, 2.1f, -2f, 2.9f)
          curveToRelative(-1f, 0.8f, -2.1f, 1.3f, -3.3f, 1.5f)
          horizontalLineTo(1.9f)
          curveToRelative(-0.4f, 0.1f, -0.6f, 0.4f, -0.6f, 0.8f)
          curveToRelative(0.1f, 0.3f, 0.3f, 0.6f, 0.6f, 0.6f)
          curveToRelative(1.6f, 0.2f, 3.2f, -0.1f, 4.7f, -0.8f)
          curveToRelative(1.4f, -0.8f, 2.7f, -1.9f, 3.6f, -3.2f)
          curveToRelative(1.7f, -2.6f, 2.9f, -5.4f, 3.6f, -8.4f)
          lineToRelative(0.1f, -0.3f)
          curveToRelative(2.5f, -9.7f, 2.5f, -19.8f, 0f, -29.5f)
          lineToRelative(-0.1f, -0.3f)
          curveToRelative(-0.7f, -3f, -1.9f, -5.8f, -3.6f, -8.4f)
          curveTo(9.3f, 3.5f, 8f, 2.4f, 6.6f, 1.6f)
          curveTo(5.1f, 0.9f, 3.5f, 0.7f, 1.9f, 0.9f)
          curveToRelative(-0.4f, 0f, -0.7f, 0.4f, -0.6f, 0.7f)
          curveTo(1.3f, 1.9f, 1.5f, 2.2f, 1.9f, 2.2f)
          lineTo(1.9f, 2.2f)
          close()
        }
      }.build()

    return icon!!
  }

private var icon: ImageVector? = null
