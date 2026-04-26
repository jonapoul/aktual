@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Subtract: ImageVector by lazy {
  aktualIcon(name = "Subtract", width = 23f, height = 3f) {
    aktualPath {
      moveTo(23f, 1.5f)
      arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21.5f, 3f)
      horizontalLineToRelative(-20f)
      arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -3f)
      horizontalLineToRelative(20f)
      arcTo(1.5f, 1.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23f, 1.5f)
      close()
    }
  }
}