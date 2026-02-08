@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.ArrowThickDown: ImageVector by lazy {
  aktualIcon(name = "ArrowThickDown", size = 20f) {
    aktualPath {
      moveTo(7f, 10f)
      verticalLineTo(2f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(8f)
      horizontalLineToRelative(5f)
      lineToRelative(-8f, 8f)
      lineToRelative(-8f, -8f)
      horizontalLineToRelative(5f)
      close()
    }
  }
}
