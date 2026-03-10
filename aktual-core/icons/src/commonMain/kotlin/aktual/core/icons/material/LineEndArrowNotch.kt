@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.LineEndArrowNotch: ImageVector by lazy {
  materialIcon(name = "LineEndArrowNotch", viewportSize = 960f) {
    materialPath {
      moveTo(440f, 760f)
      lineToRelative(137f, -240f)
      horizontalLineTo(80f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(497f)
      lineTo(440f, 200f)
      lineToRelative(440f, 280f)
      lineToRelative(-440f, 280f)
      close()
    }
  }
}
