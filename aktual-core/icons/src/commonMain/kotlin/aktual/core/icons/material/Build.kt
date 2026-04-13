@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Build: ImageVector by lazy {
  materialIcon(name = "Build", viewportSize = 960f) {
    materialPath {
      moveTo(686f, 828f)
      lineTo(444f, 584f)
      quadToRelative(-20f, 8f, -40.5f, 12f)
      reflectiveQuadToRelative(-43.5f, 4f)
      quadToRelative(-100f, 0f, -170f, -70f)
      reflectiveQuadToRelative(-70f, -170f)
      quadToRelative(0f, -36f, 10f, -68.5f)
      reflectiveQuadToRelative(28f, -61.5f)
      lineToRelative(146f, 146f)
      lineToRelative(72f, -72f)
      lineToRelative(-146f, -146f)
      quadToRelative(29f, -18f, 61.5f, -28f)
      reflectiveQuadToRelative(68.5f, -10f)
      quadToRelative(100f, 0f, 170f, 70f)
      reflectiveQuadToRelative(70f, 170f)
      quadToRelative(0f, 23f, -4f, 43.5f)
      reflectiveQuadTo(584f, 444f)
      lineToRelative(244f, 242f)
      quadToRelative(12f, 12f, 12f, 29f)
      reflectiveQuadToRelative(-12f, 29f)
      lineToRelative(-84f, 84f)
      quadToRelative(-12f, 12f, -29f, 12f)
      reflectiveQuadToRelative(-29f, -12f)
      close()
    }
  }
}
