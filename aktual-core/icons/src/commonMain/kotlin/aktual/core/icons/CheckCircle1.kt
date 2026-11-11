@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.CheckCircle1: ImageVector by lazy {
  aktualIcon(name = "CheckCircle1", size = 24f, padded = true) {
    aktualPath {
      moveTo(12f, 0f)
      arcToRelative(12f, 12f, 0f, true, false, 12f, 12f)
      arcTo(12.014f, 12.014f, 0f, false, false, 12f, 0f)
      moveToRelative(6.927f, 8.2f)
      lineToRelative(-6.845f, 9.289f)
      arcToRelative(1.01f, 1.01f, 0f, false, true, -1.43f, 0.188f)
      lineToRelative(-4.888f, -3.908f)
      arcToRelative(1f, 1f, 0f, true, true, 1.25f, -1.562f)
      lineToRelative(4.076f, 3.261f)
      lineToRelative(6.227f, -8.451f)
      arcToRelative(1f, 1f, 0f, true, true, 1.61f, 1.183f)
    }
  }
}
