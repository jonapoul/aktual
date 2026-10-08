@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Opacity: ImageVector by lazy {
  materialIcon(name = "Opacity", viewportSize = 960f) {
    materialPath {
      moveTo(253.5f, 748f)
      quadTo(160f, 656f, 160f, 524f)
      quadToRelative(0f, -65f, 25f, -121.5f)
      reflectiveQuadTo(254f, 302f)
      lineToRelative(226f, -222f)
      lineToRelative(226f, 222f)
      quadToRelative(44f, 44f, 69f, 100.5f)
      reflectiveQuadTo(800f, 524f)
      quadToRelative(0f, 132f, -93.5f, 224f)
      reflectiveQuadTo(480f, 840f)
      quadToRelative(-133f, 0f, -226.5f, -92f)
      close()
      moveTo(242f, 560f)
      horizontalLineToRelative(474f)
      quadToRelative(12f, -72f, -13.5f, -123f)
      reflectiveQuadTo(650f, 360f)
      lineTo(480f, 192f)
      lineTo(310f, 360f)
      quadToRelative(-27f, 26f, -53f, 77f)
      reflectiveQuadToRelative(-15f, 123f)
      close()
    }
  }
}
