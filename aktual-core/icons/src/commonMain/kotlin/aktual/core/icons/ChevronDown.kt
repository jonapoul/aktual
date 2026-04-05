@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.ChevronDown: ImageVector by lazy {
  aktualIcon(name = "ChevronDown", size = 20f) {
    aktualPath {
      moveTo(9.293f, 12.95f)
      lineToRelative(0.707f, 0.707f)
      lineTo(15.657f, 8f)
      lineToRelative(-1.414f, -1.414f)
      lineTo(10f, 10.828f)
      lineTo(5.757f, 6.586f)
      lineTo(4.343f, 8f)
      close()
    }
  }
}
