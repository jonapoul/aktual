@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Filter: ImageVector by lazy {
  aktualIcon(name = "Filter", size = 20f, padded = true) {
    aktualPath {
      moveTo(12f, 12f)
      lineToRelative(8f, -8f)
      verticalLineTo(0f)
      horizontalLineTo(0f)
      verticalLineToRelative(4f)
      lineToRelative(8f, 8f)
      verticalLineToRelative(8f)
      lineToRelative(4f, -4f)
      verticalLineToRelative(-4f)
      close()
    }
  }
}
