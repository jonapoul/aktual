@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ChevronRight: ImageVector by lazy {
  materialIcon(name = "ChevronRight", viewportSize = 960f, autoMirror = true) {
    materialPath {
      moveTo(504f, 480f)
      lineTo(320f, 296f)
      lineToRelative(56f, -56f)
      lineToRelative(240f, 240f)
      lineToRelative(-240f, 240f)
      lineToRelative(-56f, -56f)
      lineToRelative(184f, -184f)
      close()
    }
  }
}
