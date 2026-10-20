@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Undo: ImageVector by lazy {
  materialIcon(name = "Undo", viewportSize = 960f) {
    materialPath {
      moveTo(280f, 760f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(284f)
      quadToRelative(63f, 0f, 109.5f, -40f)
      reflectiveQuadTo(720f, 540f)
      quadToRelative(0f, -60f, -46.5f, -100f)
      reflectiveQuadTo(564f, 400f)
      horizontalLineTo(312f)
      lineToRelative(104f, 104f)
      lineToRelative(-56f, 56f)
      lineToRelative(-200f, -200f)
      lineToRelative(200f, -200f)
      lineToRelative(56f, 56f)
      lineToRelative(-104f, 104f)
      horizontalLineToRelative(252f)
      quadToRelative(97f, 0f, 166.5f, 63f)
      reflectiveQuadTo(800f, 540f)
      quadToRelative(0f, 94f, -69.5f, 157f)
      reflectiveQuadTo(564f, 760f)
      horizontalLineTo(280f)
      close()
    }
  }
}
