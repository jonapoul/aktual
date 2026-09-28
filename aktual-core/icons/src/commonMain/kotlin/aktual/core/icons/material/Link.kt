@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Link: ImageVector by lazy {
  materialIcon(name = "Link", viewportSize = 960f) {
    materialPath {
      moveTo(440f, 680f)
      horizontalLineTo(280f)
      quadToRelative(-83f, 0f, -141.5f, -58.5f)
      reflectiveQuadTo(80f, 480f)
      quadToRelative(0f, -83f, 58.5f, -141.5f)
      reflectiveQuadTo(280f, 280f)
      horizontalLineToRelative(160f)
      verticalLineToRelative(80f)
      horizontalLineTo(280f)
      quadToRelative(-50f, 0f, -85f, 35f)
      reflectiveQuadToRelative(-35f, 85f)
      quadToRelative(0f, 50f, 35f, 85f)
      reflectiveQuadToRelative(85f, 35f)
      horizontalLineToRelative(160f)
      verticalLineToRelative(80f)
      close()
      moveTo(320f, 520f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(320f)
      verticalLineToRelative(80f)
      horizontalLineTo(320f)
      close()
      moveToRelative(200f, 160f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(160f)
      quadToRelative(50f, 0f, 85f, -35f)
      reflectiveQuadToRelative(35f, -85f)
      quadToRelative(0f, -50f, -35f, -85f)
      reflectiveQuadToRelative(-85f, -35f)
      horizontalLineTo(520f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(160f)
      quadToRelative(83f, 0f, 141.5f, 58.5f)
      reflectiveQuadTo(880f, 480f)
      quadToRelative(0f, 83f, -58.5f, 141.5f)
      reflectiveQuadTo(680f, 680f)
      horizontalLineTo(520f)
      close()
    }
  }
}
