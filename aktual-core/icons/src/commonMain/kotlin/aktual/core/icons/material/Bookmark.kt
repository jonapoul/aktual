@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Bookmark: ImageVector by lazy {
  materialIcon(name = "Bookmark", viewportSize = 960f) {
    materialPath {
      moveTo(200f, 840f)
      verticalLineToRelative(-640f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(280f, 120f)
      horizontalLineToRelative(400f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(760f, 200f)
      verticalLineToRelative(640f)
      lineTo(480f, 720f)
      lineTo(200f, 840f)
      close()
    }
  }
}
