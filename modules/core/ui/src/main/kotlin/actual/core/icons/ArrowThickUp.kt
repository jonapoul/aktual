@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package actual.core.icons

import actual.core.ui.PreviewActualRow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val ActualIcons.ArrowThickUp: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = ImageVector.Builder(
      name = "ArrowThickUp",
      defaultWidth = 20.dp,
      defaultHeight = 20.dp,
      viewportWidth = 20f,
      viewportHeight = 20f,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(7f, 10f)
        verticalLineToRelative(8f)
        horizontalLineToRelative(6f)
        verticalLineToRelative(-8f)
        horizontalLineToRelative(5f)
        lineToRelative(-8f, -8f)
        lineToRelative(-8f, 8f)
        horizontalLineToRelative(5f)
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
    imageVector = ActualIcons.ArrowThickUp,
    contentDescription = null,
  )
}
