@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.CheveronRight: ImageVector by lazy {
  aktualIcon(name = "CheveronRight", size = 20f) {
    aktualPath {
      moveTo(12.95f, 10.707f)
      lineToRelative(0.707f, -0.707f)
      lineTo(8f, 4.343f)
      lineTo(6.586f, 5.757f)
      lineTo(10.828f, 10f)
      lineToRelative(-4.242f, 4.243f)
      lineTo(8f, 15.657f)
      lineToRelative(4.95f, -4.95f)
      close()
    }
  }
}
