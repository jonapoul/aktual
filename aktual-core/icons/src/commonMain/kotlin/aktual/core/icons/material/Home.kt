@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Home: ImageVector by lazy {
  materialIcon(name = "Home", viewportSize = 960f) {
    materialPath {
      moveTo(160f, 840f)
      verticalLineToRelative(-480f)
      lineToRelative(320f, -240f)
      lineToRelative(320f, 240f)
      verticalLineToRelative(480f)
      horizontalLineTo(560f)
      verticalLineToRelative(-280f)
      horizontalLineTo(400f)
      verticalLineToRelative(280f)
      horizontalLineTo(160f)
      close()
    }
  }
}
