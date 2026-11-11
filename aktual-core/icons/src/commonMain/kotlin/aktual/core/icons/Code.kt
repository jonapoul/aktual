@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Code: ImageVector by lazy {
  aktualIcon(name = "Code", size = 20f) {
    aktualPath {
      moveTo(0.7f, 9.3f)
      lineToRelative(4.8f, -4.8f)
      lineToRelative(1.4f, 1.42f)
      lineTo(2.84f, 10f)
      lineToRelative(4.07f, 4.07f)
      lineToRelative(-1.41f, 1.42f)
      lineTo(0f, 10f)
      lineToRelative(0.7f, -0.7f)
      close()
      moveTo(19.3f, 10.7f)
      lineToRelative(0.7f, -0.7f)
      lineToRelative(-5.49f, -5.49f)
      lineToRelative(-1.4f, 1.42f)
      lineTo(17.16f, 10f)
      lineToRelative(-4.07f, 4.07f)
      lineToRelative(1.41f, 1.42f)
      lineToRelative(4.78f, -4.78f)
      close()
    }
  }
}
