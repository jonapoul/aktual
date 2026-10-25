@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.DotsHorizontalTriple: ImageVector by lazy {
  aktualIcon(name = "DotsHorizontalTriple", size = 20f) {
    aktualPath {
      moveTo(10f, 12f)
      arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
      arcToRelative(2f, 2f, 0f, false, true, 0f, 4f)
      close()
      moveToRelative(0f, -6f)
      arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
      arcToRelative(2f, 2f, 0f, false, true, 0f, 4f)
      close()
      moveToRelative(0f, 12f)
      arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
      arcToRelative(2f, 2f, 0f, false, true, 0f, 4f)
      close()
    }
  }
}
