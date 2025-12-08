@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AktualIcons.Equals: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = ImageVector
      .Builder(
        name = "Equals",
        defaultWidth = 23.dp,
        defaultHeight = 11.dp,
        viewportWidth = 23f,
        viewportHeight = 11f,
      ).apply {
        path(fill = SolidColor(Color.Black)) {
          moveTo(23f, 1.5f)
          arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.5f, 3f)
          horizontalLineToRelative(-20f)
          arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -3f)
          horizontalLineToRelative(20f)
          arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23f, 1.5f)
          close()
          moveTo(23f, 9.5f)
          arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.5f, 1.5f)
          horizontalLineToRelative(-20f)
          arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -3f)
          horizontalLineToRelative(20f)
          arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23f, 9.5f)
          close()
        }
      }.build()

    return icon!!
  }

private var icon: ImageVector? = null
