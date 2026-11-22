@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Download: ImageVector by lazy {
  aktualIcon(name = "Download", size = 20f, padded = true) {
    aktualPath {
      moveTo(13f, 8f)
      verticalLineTo(2f)
      horizontalLineTo(7f)
      verticalLineToRelative(6f)
      horizontalLineTo(2f)
      lineToRelative(8f, 8f)
      lineToRelative(8f, -8f)
      horizontalLineToRelative(-5f)
      close()
      moveTo(0f, 18f)
      horizontalLineToRelative(20f)
      verticalLineToRelative(2f)
      horizontalLineTo(0f)
      verticalLineToRelative(-2f)
      close()
    }
  }
}
