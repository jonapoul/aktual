@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Menu: ImageVector by lazy {
  materialIcon(name = "Menu") {
    materialPath {
      moveTo(3.0f, 18.0f)
      horizontalLineToRelative(18.0f)
      verticalLineToRelative(-2.0f)
      lineTo(3.0f, 16.0f)
      verticalLineToRelative(2.0f)
      close()
      moveTo(3.0f, 13.0f)
      horizontalLineToRelative(18.0f)
      verticalLineToRelative(-2.0f)
      lineTo(3.0f, 11.0f)
      verticalLineToRelative(2.0f)
      close()
      moveTo(3.0f, 6.0f)
      verticalLineToRelative(2.0f)
      horizontalLineToRelative(18.0f)
      lineTo(21.0f, 6.0f)
      lineTo(3.0f, 6.0f)
      close()
    }
  }
}
