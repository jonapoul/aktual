@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.ChevronUp: ImageVector by lazy {
  aktualIcon(name = "ChevronUp", size = 20f) {
    aktualPath {
      moveTo(10.707f, 7.05f)
      lineTo(10f, 6.343f)
      lineTo(4.343f, 12f)
      lineToRelative(1.414f, 1.414f)
      lineTo(10f, 9.172f)
      lineToRelative(4.243f, 4.242f)
      lineTo(15.657f, 12f)
      close()
    }
  }
}
