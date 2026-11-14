@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Bolt: ImageVector by lazy {
  aktualIcon(name = "Bolt", size = 20f, padded = true) {
    aktualPath {
      moveTo(13f, 8f)
      verticalLineTo(0f)
      lineTo(8.11f, 5.87f)
      lineTo(3f, 12f)
      horizontalLineToRelative(4f)
      verticalLineToRelative(8f)
      lineTo(17f, 8f)
      horizontalLineToRelative(-4f)
      close()
    }
  }
}
