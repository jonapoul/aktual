@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AktualIcons.Sum: ImageVector
  get() {
    if (icon != null) {
      return requireNotNull(icon)
    }
    icon = ImageVector
      .Builder(
        name = "Sum",
        defaultWidth = 28.3.dp,
        defaultHeight = 28.3.dp,
        viewportWidth = 28.3f,
        viewportHeight = 28.3f,
      ).apply {
        path(fill = SolidColor(Color.Black)) {
          moveTo(23.2f, 10.1f)
          curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
          verticalLineTo(2.2f)
          curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
          horizontalLineToRelative(-18f)
          curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
          curveToRelative(0f, 0.4f, 0.1f, 0.9f, 0.4f, 1.2f)
          lineToRelative(8.1f, 10.8f)
          lineTo(3.6f, 25f)
          curveToRelative(-0.7f, 0.9f, -0.5f, 2.1f, 0.4f, 2.8f)
          curveToRelative(0.3f, 0.3f, 0.8f, 0.4f, 1.2f, 0.4f)
          horizontalLineToRelative(18f)
          curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
          verticalLineToRelative(-5.8f)
          curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
          reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f)
          verticalLineToRelative(3.8f)
          horizontalLineToRelative(-12f)
          lineToRelative(6.6f, -8.8f)
          curveToRelative(0.5f, -0.7f, 0.5f, -1.7f, 0f, -2.4f)
          lineTo(9.2f, 4.2f)
          horizontalLineToRelative(12f)
          verticalLineToRelative(3.9f)
          curveTo(21.2f, 9.2f, 22.1f, 10.1f, 23.2f, 10.1f)
          close()
        }
      }.build()

    return requireNotNull(icon)
  }

private var icon: ImageVector? = null
