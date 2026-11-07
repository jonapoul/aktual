@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Location: ImageVector by lazy {
  aktualIcon(name = "Location", size = 20f) {
    aktualPath {
      moveTo(10f, 20f)
      reflectiveCurveTo(3f, 10.87f, 3f, 7f)
      arcToRelative(7f, 7f, 0f, true, true, 14f, 0f)
      curveToRelative(0f, 3.87f, -7f, 13f, -7f, 13f)
      close()
      moveToRelative(0f, -11f)
      arcToRelative(2f, 2f, 0f, true, false, 0f, -4f)
      arcToRelative(2f, 2f, 0f, false, false, 0f, 4f)
      close()
    }
  }
}
