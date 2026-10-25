@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Check: ImageVector by lazy {
  aktualIcon(name = "Check", size = 14f) {
    aktualPath {
      moveTo(3.994f, 12.5f)
      arcToRelative(1.988f, 1.988f, 0f, false, true, -1.553f, -0.781f)
      lineTo(0.248f, 9.226f)
      arcToRelative(1f, 1f, 0f, false, true, 1.5f, -1.32f)
      lineToRelative(2.055f, 2.339f)
      arcToRelative(0.25f, 0.25f, 0f, false, false, 0.368f, 0.008f)
      lineToRelative(8.1f, -8.442f)
      arcToRelative(1f, 1f, 0f, true, true, 1.45f, 1.378f)
      lineTo(5.519f, 11.822f)
      arcTo(1.941f, 1.941f, 0f, false, true, 3.994f, 12.5f)
      close()
    }
  }
}
