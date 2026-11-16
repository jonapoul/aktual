@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Square: ImageVector by lazy {
  materialIcon(name = "Square", viewportSize = 960f) {
    materialPath {
      moveTo(120f, 840f)
      verticalLineToRelative(-720f)
      horizontalLineToRelative(720f)
      verticalLineToRelative(720f)
      horizontalLineTo(120f)
      close()
    }
  }
}
