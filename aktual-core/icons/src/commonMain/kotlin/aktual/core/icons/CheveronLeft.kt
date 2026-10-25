@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.CheveronLeft: ImageVector by lazy {
  aktualIcon(name = "CheveronLeft", size = 20f) {
    aktualPath {
      moveTo(7.05f, 9.293f)
      lineTo(6.343f, 10f)
      lineTo(12f, 15.657f)
      lineToRelative(1.414f, -1.414f)
      lineTo(9.172f, 10f)
      lineToRelative(4.242f, -4.243f)
      lineTo(12f, 4.343f)
      close()
    }
  }
}
