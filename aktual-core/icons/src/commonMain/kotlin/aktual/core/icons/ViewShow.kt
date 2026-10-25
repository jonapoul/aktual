@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.ViewShow: ImageVector by lazy {
  aktualIcon(name = "ViewShow", size = 20f) {
    aktualPath {
      moveTo(0.2f, 10f)
      arcToRelative(11f, 11f, 0f, false, true, 19.6f, 0f)
      arcTo(11f, 11f, 0f, false, true, 0.2f, 10f)
      close()
      moveToRelative(9.8f, 4f)
      arcToRelative(4f, 4f, 0f, true, false, 0f, -8f)
      arcToRelative(4f, 4f, 0f, false, false, 0f, 8f)
      close()
      moveToRelative(0f, -2f)
      arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
      arcToRelative(2f, 2f, 0f, false, true, 0f, 4f)
      close()
    }
  }
}
