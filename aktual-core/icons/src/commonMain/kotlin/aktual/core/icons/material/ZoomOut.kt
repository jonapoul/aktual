@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ZoomOut: ImageVector by lazy {
  materialIcon(name = "ZoomOut", viewportSize = 960f) {
    materialPath {
      moveTo(784f, 840f)
      lineTo(532f, 588f)
      quadToRelative(-30f, 24f, -69f, 38f)
      reflectiveQuadToRelative(-83f, 14f)
      quadToRelative(-109f, 0f, -184.5f, -75.5f)
      reflectiveQuadTo(120f, 380f)
      quadToRelative(0f, -109f, 75.5f, -184.5f)
      reflectiveQuadTo(380f, 120f)
      quadToRelative(109f, 0f, 184.5f, 75.5f)
      reflectiveQuadTo(640f, 380f)
      quadToRelative(0f, 44f, -14f, 83f)
      reflectiveQuadToRelative(-38f, 69f)
      lineToRelative(252f, 252f)
      lineToRelative(-56f, 56f)
      close()
      moveTo(380f, 560f)
      quadToRelative(75f, 0f, 127.5f, -52.5f)
      reflectiveQuadTo(560f, 380f)
      quadToRelative(0f, -75f, -52.5f, -127.5f)
      reflectiveQuadTo(380f, 200f)
      quadToRelative(-75f, 0f, -127.5f, 52.5f)
      reflectiveQuadTo(200f, 380f)
      quadToRelative(0f, 75f, 52.5f, 127.5f)
      reflectiveQuadTo(380f, 560f)
      close()
      moveTo(280f, 420f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(200f)
      verticalLineToRelative(80f)
      horizontalLineTo(280f)
      close()
    }
  }
}
