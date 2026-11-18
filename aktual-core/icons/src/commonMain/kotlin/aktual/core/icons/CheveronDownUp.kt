@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.CheveronDownUp: ImageVector by lazy {
  aktualIcon(name = "CheveronDownUp", size = 20f) {
    aktualPath {
      moveTo(10f, 8.24f)
      lineTo(4.99f, 3.23f)
      lineTo(6.42f, 1.8f)
      lineTo(10f, 5.38f)
      lineToRelative(3.58f, -3.58f)
      lineToRelative(1.43f, 1.43f)
      close()
      moveToRelative(0f, 3.52f)
      lineToRelative(5.01f, 5.01f)
      lineToRelative(-1.43f, 1.43f)
      lineTo(10f, 14.62f)
      lineTo(6.42f, 18.2f)
      lineToRelative(-1.43f, -1.43f)
      close()
    }
  }
}
