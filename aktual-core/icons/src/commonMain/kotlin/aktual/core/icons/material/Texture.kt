@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Texture: ImageVector by lazy {
  materialIcon(name = "Texture", viewportSize = 960f) {
    materialPath {
      moveTo(176f, 840f)
      quadToRelative(-19f, -4f, -35.5f, -20.5f)
      reflectiveQuadTo(120f, 784f)
      lineToRelative(664f, -664f)
      quadToRelative(21f, 5f, 36f, 20.5f)
      reflectiveQuadToRelative(21f, 35.5f)
      lineTo(176f, 840f)
      close()
      moveToRelative(-56f, -252f)
      verticalLineToRelative(-112f)
      lineToRelative(356f, -356f)
      horizontalLineToRelative(112f)
      lineTo(120f, 588f)
      close()
      moveToRelative(0f, -308f)
      verticalLineToRelative(-80f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(200f, 120f)
      horizontalLineToRelative(80f)
      lineTo(120f, 280f)
      close()
      moveToRelative(560f, 560f)
      lineToRelative(160f, -160f)
      verticalLineToRelative(80f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(760f, 840f)
      horizontalLineToRelative(-80f)
      close()
      moveToRelative(-308f, 0f)
      lineToRelative(468f, -468f)
      verticalLineToRelative(112f)
      lineTo(484f, 840f)
      horizontalLineTo(372f)
      close()
    }
  }
}
