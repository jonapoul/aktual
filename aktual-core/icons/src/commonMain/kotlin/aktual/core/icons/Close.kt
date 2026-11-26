@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Close: ImageVector by lazy {
  aktualIcon(name = "Close", size = 20f, padded = true) {
    aktualPath {
      moveTo(10f, 8.586f)
      lineTo(2.929f, 1.515f)
      lineTo(1.515f, 2.929f)
      lineTo(8.586f, 10f)
      lineToRelative(-7.071f, 7.071f)
      lineToRelative(1.414f, 1.414f)
      lineTo(10f, 11.414f)
      lineToRelative(7.071f, 7.071f)
      lineToRelative(1.414f, -1.414f)
      lineTo(11.414f, 10f)
      lineToRelative(7.071f, -7.071f)
      lineToRelative(-1.414f, -1.414f)
      lineTo(10f, 8.586f)
      close()
    }
  }
}
