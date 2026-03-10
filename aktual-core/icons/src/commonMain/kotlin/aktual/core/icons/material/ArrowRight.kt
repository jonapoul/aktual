@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ArrowRight: ImageVector by lazy {
  materialIcon(name = "ArrowRight", autoMirror = true) {
    materialPath {
      moveTo(10f, 17f)
      lineToRelative(5f, -5f)
      lineToRelative(-5f, -5f)
      verticalLineToRelative(10f)
      close()
    }
  }
}
