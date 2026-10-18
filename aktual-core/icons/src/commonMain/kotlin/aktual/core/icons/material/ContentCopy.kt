@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ContentCopy: ImageVector by lazy {
  materialIcon(name = "ContentCopy", viewportSize = 960f) {
    materialPath {
      moveTo(360f, 720f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(280f, 640f)
      verticalLineToRelative(-480f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(360f, 80f)
      horizontalLineToRelative(360f)
      quadToRelative(33f, 0f, 56.5f, 23.5f)
      reflectiveQuadTo(800f, 160f)
      verticalLineToRelative(480f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(720f, 720f)
      horizontalLineTo(360f)
      close()
      moveTo(200f, 880f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(120f, 800f)
      verticalLineToRelative(-560f)
      horizontalLineToRelative(80f)
      verticalLineToRelative(560f)
      horizontalLineToRelative(440f)
      verticalLineToRelative(80f)
      horizontalLineTo(200f)
      close()
    }
  }
}
