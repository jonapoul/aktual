@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Window: ImageVector by lazy {
  materialIcon(name = "Window", viewportSize = 960f) {
    materialPath {
      moveTo(520f, 520f)
      horizontalLineToRelative(320f)
      verticalLineToRelative(240f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(760f, 840f)
      horizontalLineTo(520f)
      verticalLineToRelative(-320f)
      close()
      moveToRelative(0f, -80f)
      verticalLineToRelative(-320f)
      horizontalLineToRelative(240f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(840f, 200f)
      verticalLineToRelative(240f)
      horizontalLineTo(520f)
      close()
      moveToRelative(-80f, 0f)
      horizontalLineTo(120f)
      verticalLineToRelative(-240f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(200f, 120f)
      horizontalLineToRelative(240f)
      verticalLineToRelative(320f)
      close()
      moveToRelative(0f, 80f)
      verticalLineToRelative(320f)
      horizontalLineTo(200f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(120f, 760f)
      verticalLineToRelative(-240f)
      horizontalLineToRelative(320f)
      close()
    }
  }
}
