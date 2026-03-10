@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.DarkMode: ImageVector by lazy {
  materialIcon(name = "DarkMode", viewportSize = 960f) {
    materialPath {
      moveTo(480f, 840f)
      quadToRelative(-150f, 0f, -255f, -105f)
      reflectiveQuadTo(120f, 480f)
      quadToRelative(0f, -150f, 105f, -255f)
      reflectiveQuadToRelative(255f, -105f)
      quadToRelative(14f, 0f, 27.5f, 1f)
      reflectiveQuadToRelative(26.5f, 3f)
      quadToRelative(-41f, 29f, -65.5f, 75.5f)
      reflectiveQuadTo(444f, 300f)
      quadToRelative(0f, 90f, 63f, 153f)
      reflectiveQuadToRelative(153f, 63f)
      quadToRelative(55f, 0f, 101f, -24.5f)
      reflectiveQuadToRelative(75f, -65.5f)
      quadToRelative(2f, 13f, 3f, 26.5f)
      reflectiveQuadToRelative(1f, 27.5f)
      quadToRelative(0f, 150f, -105f, 255f)
      reflectiveQuadTo(480f, 840f)
      close()
    }
  }
}
