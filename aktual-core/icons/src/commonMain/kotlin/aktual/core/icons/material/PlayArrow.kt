@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.PlayArrow: ImageVector by lazy {
  materialIcon(name = "PlayArrow", viewportSize = 960f) {
    materialPath {
      moveTo(320f, 760f)
      verticalLineToRelative(-560f)
      lineToRelative(440f, 280f)
      lineToRelative(-440f, 280f)
      close()
    }
  }
}
