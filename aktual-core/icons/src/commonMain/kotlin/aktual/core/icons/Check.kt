@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Check: ImageVector by lazy {
  materialIcon(name = "Check") {
    materialPath {
      moveTo(9.0f, 16.17f)
      lineTo(4.83f, 12.0f)
      lineToRelative(-1.42f, 1.41f)
      lineTo(9.0f, 19.0f)
      lineTo(21.0f, 7.0f)
      lineToRelative(-1.41f, -1.41f)
      close()
    }
  }
}
