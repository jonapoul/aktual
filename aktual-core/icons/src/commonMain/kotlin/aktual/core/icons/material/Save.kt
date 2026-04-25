@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Save: ImageVector by lazy {
  materialIcon(name = "Save", viewportSize = 960f) {
    materialPath {
      moveTo(840f, 280f)
      verticalLineToRelative(480f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(760f, 840f)
      horizontalLineTo(200f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(120f, 760f)
      verticalLineToRelative(-560f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(200f, 120f)
      horizontalLineToRelative(480f)
      lineToRelative(160f, 160f)
      close()
      moveTo(565f, 685f)
      quadToRelative(35f, -35f, 35f, -85f)
      reflectiveQuadToRelative(-35f, -85f)
      quadToRelative(-35f, -35f, -85f, -35f)
      reflectiveQuadToRelative(-85f, 35f)
      quadToRelative(-35f, 35f, -35f, 85f)
      reflectiveQuadToRelative(35f, 85f)
      quadToRelative(35f, 35f, 85f, 35f)
      reflectiveQuadToRelative(85f, -35f)
      close()
      moveTo(240f, 400f)
      horizontalLineToRelative(360f)
      verticalLineToRelative(-160f)
      horizontalLineTo(240f)
      verticalLineToRelative(160f)
      close()
    }
  }
}
