@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.CropSquare: ImageVector by lazy {
  materialIcon(name = "CropSquare", viewportSize = 960f) {
    materialPath {
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
      moveToRelative(0f, -80f)
      horizontalLineToRelative(560f)
      verticalLineToRelative(-560f)
      horizontalLineTo(200f)
      verticalLineToRelative(560f)
      close()
      moveToRelative(0f, 0f)
      verticalLineToRelative(-560f)
      verticalLineToRelative(560f)
      close()
    }
  }
}
