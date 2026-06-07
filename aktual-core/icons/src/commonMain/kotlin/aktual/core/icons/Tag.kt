@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Tag: ImageVector by lazy {
  aktualIcon(name = "Tag", size = 20f) {
    aktualPath {
      moveTo(0f, 10f)
      verticalLineTo(2f)
      lineToRelative(2f, -2f)
      horizontalLineToRelative(8f)
      lineToRelative(10f, 10f)
      lineToRelative(-10f, 10f)
      lineTo(0f, 10f)
      close()
      moveToRelative(4.5f, -4f)
      arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, -3f)
      arcToRelative(1.5f, 1.5f, 0f, false, false, 0f, 3f)
      close()
    }
  }
}
