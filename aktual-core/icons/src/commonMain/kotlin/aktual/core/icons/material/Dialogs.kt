@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Dialogs: ImageVector by lazy {
  materialIcon(name = "Dialogs", viewportSize = 960f) {
    materialPath {
      // Inner rectangle
      moveTo(320f, 640f)
      horizontalLineToRelative(320f)
      verticalLineToRelative(-320f)
      horizontalLineTo(320f)
      verticalLineToRelative(320f)
      close()
      // Outer rounded rectangle
      moveTo(200f, 840f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(120f, 760f)
      verticalLineToRelative(-560f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(200f, 120f)
      horizontalLineToRelative(560f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(840f, 200f)
      verticalLineToRelative(560f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(760f, 840f)
      horizontalLineTo(200f)
      close()
    }
  }
}
