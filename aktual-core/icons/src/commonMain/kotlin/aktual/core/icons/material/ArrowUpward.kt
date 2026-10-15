@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ArrowUpward: ImageVector by lazy {
  materialIcon(name = "ArrowUpward", viewportSize = 960f) {
    materialPath {
      moveTo(440f, 800f)
      verticalLineToRelative(-487f)
      lineTo(216f, 537f)
      lineToRelative(-56f, -57f)
      lineToRelative(320f, -320f)
      lineToRelative(320f, 320f)
      lineToRelative(-56f, 57f)
      lineToRelative(-224f, -224f)
      verticalLineToRelative(487f)
      horizontalLineToRelative(-80f)
      close()
    }
  }
}
