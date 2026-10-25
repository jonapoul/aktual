@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Refresh: ImageVector by lazy {
  aktualIcon(name = "Refresh", size = 20f) {
    aktualPath {
      moveTo(10f, 3f)
      verticalLineToRelative(2f)
      arcToRelative(5f, 5f, 0f, false, false, -3.54f, 8.54f)
      lineToRelative(-1.41f, 1.41f)
      arcTo(7f, 7f, 0f, false, true, 10f, 3f)
      close()
      moveToRelative(4.95f, 2.05f)
      arcTo(7f, 7f, 0f, false, true, 10f, 17f)
      verticalLineToRelative(-2f)
      arcToRelative(5f, 5f, 0f, false, false, 3.54f, -8.54f)
      lineToRelative(1.41f, -1.41f)
      close()
      moveTo(10f, 20f)
      lineToRelative(-4f, -4f)
      lineToRelative(4f, -4f)
      verticalLineToRelative(8f)
      close()
      moveToRelative(0f, -12f)
      verticalLineTo(0f)
      lineToRelative(4f, 4f)
      lineToRelative(-4f, 4f)
      close()
    }
  }
}
