@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ArrowDropDown: ImageVector by lazy {
  materialIcon(name = "ArrowDropDown", viewportSize = 960f) {
    materialPath {
      moveTo(480f, 600f)
      lineTo(280f, 400f)
      horizontalLineToRelative(400f)
      lineTo(480f, 600f)
      close()
    }
  }
}
