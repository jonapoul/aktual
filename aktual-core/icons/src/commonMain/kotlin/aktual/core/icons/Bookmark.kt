@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Bookmark: ImageVector by lazy {
  aktualIcon(name = "Bookmark", size = 20f) {
    aktualPath {
      moveTo(2f, 2f)
      curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
      horizontalLineToRelative(12f)
      arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
      verticalLineToRelative(18f)
      lineToRelative(-8f, -4f)
      lineToRelative(-8f, 4f)
      verticalLineTo(2f)
      close()
    }
  }
}
