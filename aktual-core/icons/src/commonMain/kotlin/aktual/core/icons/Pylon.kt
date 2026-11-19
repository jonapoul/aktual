@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Pylon: ImageVector by lazy {
  aktualIcon(name = "Pylon", size = 20f, padded = true) {
    aktualPath {
      moveTo(17.4f, 18f)
      horizontalLineTo(20f)
      verticalLineToRelative(2f)
      horizontalLineTo(0f)
      verticalLineToRelative(-2f)
      horizontalLineToRelative(2.6f)
      lineTo(8f, 0f)
      horizontalLineToRelative(4f)
      close()
      moveToRelative(-3.2f, -4f)
      horizontalLineTo(5.8f)
      lineToRelative(-1.2f, 4f)
      horizontalLineToRelative(10.8f)
      close()
      moveToRelative(-2.4f, -8f)
      horizontalLineTo(8.2f)
      lineTo(7f, 10f)
      horizontalLineToRelative(6f)
      close()
    }
  }
}
