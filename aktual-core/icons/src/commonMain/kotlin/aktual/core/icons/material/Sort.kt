@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Sort: ImageVector by lazy {
  materialIcon(name = "Sort", viewportSize = 960f) {
    materialPath {
      moveTo(120f, 720f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(240f)
      verticalLineToRelative(80f)
      horizontalLineTo(120f)
      close()
      moveToRelative(0f, -200f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(480f)
      verticalLineToRelative(80f)
      horizontalLineTo(120f)
      close()
      moveToRelative(0f, -200f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(720f)
      verticalLineToRelative(80f)
      horizontalLineTo(120f)
      close()
    }
  }
}
