@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Wallet: ImageVector by lazy {
  aktualIcon(name = "Wallet", size = 20f) {
    aktualPath {
      moveTo(0f, 4f)
      curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
      horizontalLineToRelative(15f)
      arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
      verticalLineToRelative(1f)
      horizontalLineTo(2f)
      verticalLineToRelative(1f)
      horizontalLineToRelative(17f)
      arcToRelative(1f, 1f, 0f, false, true, 1f, 1f)
      verticalLineToRelative(10f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      horizontalLineTo(2f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
      verticalLineTo(4f)
      close()
      moveToRelative(16.5f, 9f)
      arcToRelative(1.5f, 1.5f, 0f, true, false, 0f, -3f)
      arcToRelative(1.5f, 1.5f, 0f, false, false, 0f, 3f)
      close()
    }
  }
}
