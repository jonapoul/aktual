@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.SpaceBar: ImageVector by lazy {
  materialIcon(name = "SpaceBar") {
    materialPath {
      moveTo(18f, 9f)
      verticalLineToRelative(4f)
      horizontalLineTo(6f)
      verticalLineTo(9f)
      horizontalLineTo(4f)
      verticalLineToRelative(6f)
      horizontalLineToRelative(16f)
      verticalLineTo(9f)
      close()
    }
  }
}
