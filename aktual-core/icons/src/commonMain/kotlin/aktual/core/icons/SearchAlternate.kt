@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.SearchAlternate: ImageVector by lazy {
  aktualIcon(name = "SearchAlternate", size = 24f) {
    aktualPath {
      moveTo(23.384f, 21.619f)
      lineTo(16.855f, 15.09f)
      arcToRelative(9.284f, 9.284f, 0f, true, false, -1.768f, 1.768f)
      lineToRelative(6.529f, 6.529f)
      arcToRelative(1.266f, 1.266f, 0f, false, false, 1.768f, 0f)
      arcTo(1.251f, 1.251f, 0f, false, false, 23.384f, 21.619f)
      close()
      moveTo(2.75f, 9.5f)
      arcToRelative(6.75f, 6.75f, 0f, true, true, 6.75f, 6.75f)
      arcTo(6.758f, 6.758f, 0f, false, true, 2.75f, 9.5f)
      close()
    }
  }
}
