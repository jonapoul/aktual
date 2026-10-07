@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ExpandMore: ImageVector by lazy {
  materialIcon(name = "ExpandMore") {
    materialPath {
      moveTo(16.59f, 8.59f)
      lineTo(12f, 13.17f)
      lineTo(7.41f, 8.59f)
      lineTo(6f, 10f)
      lineToRelative(6f, 6f)
      lineToRelative(6f, -6f)
      close()
    }
  }
}
