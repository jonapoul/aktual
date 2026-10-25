@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.LockClosed: ImageVector by lazy {
  aktualIcon(name = "LockClosed", size = 20f) {
    aktualPath {
      moveTo(4f, 8f)
      verticalLineTo(6f)
      arcToRelative(6f, 6f, 0f, true, true, 12f, 0f)
      verticalLineToRelative(2f)
      horizontalLineToRelative(1f)
      arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
      verticalLineToRelative(8f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      horizontalLineTo(3f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
      verticalLineToRelative(-8f)
      curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
      horizontalLineToRelative(1f)
      close()
      moveToRelative(5f, 6.73f)
      verticalLineTo(17f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(-2.27f)
      arcToRelative(2f, 2f, 0f, true, false, -2f, 0f)
      close()
      moveTo(7f, 6f)
      verticalLineToRelative(2f)
      horizontalLineToRelative(6f)
      verticalLineTo(6f)
      arcToRelative(3f, 3f, 0f, false, false, -6f, 0f)
      close()
    }
  }
}
