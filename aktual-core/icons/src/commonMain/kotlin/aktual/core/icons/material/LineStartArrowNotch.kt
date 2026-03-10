@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.LineStartArrowNotch: ImageVector by lazy {
  materialIcon(name = "LineStartArrowNotch", viewportSize = 960f) {
    materialPath {
      moveTo(520f, 760f)
      lineTo(80f, 480f)
      lineToRelative(440f, -280f)
      lineToRelative(-137f, 240f)
      horizontalLineToRelative(497f)
      verticalLineToRelative(80f)
      horizontalLineTo(383f)
      lineToRelative(137f, 240f)
      close()
    }
  }
}
