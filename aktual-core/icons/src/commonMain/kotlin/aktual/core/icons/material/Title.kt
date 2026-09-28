@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Title: ImageVector by lazy {
  materialIcon(name = "Title", viewportSize = 960f) {
    materialPath {
      moveTo(420f, 800f)
      verticalLineToRelative(-520f)
      horizontalLineTo(200f)
      verticalLineToRelative(-120f)
      horizontalLineToRelative(560f)
      verticalLineToRelative(120f)
      horizontalLineTo(540f)
      verticalLineToRelative(520f)
      horizontalLineTo(420f)
      close()
    }
  }
}
