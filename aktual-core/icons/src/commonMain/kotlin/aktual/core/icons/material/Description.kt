@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Description: ImageVector by lazy {
  materialIcon(name = "Description", viewportSize = 960f) {
    materialPath {
      moveTo(320f, 720f)
      horizontalLineToRelative(320f)
      verticalLineToRelative(-80f)
      horizontalLineTo(320f)
      verticalLineToRelative(80f)
      close()
      moveToRelative(0f, -160f)
      horizontalLineToRelative(320f)
      verticalLineToRelative(-80f)
      horizontalLineTo(320f)
      verticalLineToRelative(80f)
      close()
      moveTo(240f, 880f)
      quadToRelative(-33f, 0f, -56.5f, -23.5f)
      reflectiveQuadTo(160f, 800f)
      verticalLineToRelative(-640f)
      quadToRelative(0f, -33f, 23.5f, -56.5f)
      reflectiveQuadTo(240f, 80f)
      horizontalLineToRelative(320f)
      lineToRelative(240f, 240f)
      verticalLineToRelative(480f)
      quadToRelative(0f, 33f, -23.5f, 56.5f)
      reflectiveQuadTo(720f, 880f)
      horizontalLineTo(240f)
      close()
      moveToRelative(280f, -520f)
      horizontalLineToRelative(200f)
      lineTo(520f, 160f)
      verticalLineToRelative(200f)
      close()
    }
  }
}
