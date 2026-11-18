@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.CheveronUpDown: ImageVector by lazy {
  aktualIcon(name = "CheveronUpDown", size = 20f) {
    aktualPath {
      moveTo(10f, 1.8f)
      lineToRelative(5.01f, 5.01f)
      lineToRelative(-1.43f, 1.43f)
      lineTo(10f, 4.66f)
      lineTo(6.42f, 8.24f)
      lineTo(4.99f, 6.81f)
      close()
      moveToRelative(0f, 16.4f)
      lineToRelative(-5.01f, -5.01f)
      lineToRelative(1.43f, -1.43f)
      lineTo(10f, 15.34f)
      lineToRelative(3.58f, -3.58f)
      lineToRelative(1.43f, 1.43f)
      close()
    }
  }
}
