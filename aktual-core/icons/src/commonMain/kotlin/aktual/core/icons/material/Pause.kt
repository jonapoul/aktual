@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Pause: ImageVector by lazy {
  materialIcon(name = "Pause", viewportSize = 960f) {
    materialPath {
      moveTo(560f, 760f)
      verticalLineToRelative(-560f)
      horizontalLineToRelative(160f)
      verticalLineToRelative(560f)
      horizontalLineTo(560f)
      close()
      moveToRelative(-320f, 0f)
      verticalLineToRelative(-560f)
      horizontalLineToRelative(160f)
      verticalLineToRelative(560f)
      horizontalLineTo(240f)
      close()
    }
  }
}
