@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.ClearAll: ImageVector by lazy {
  materialIcon(name = "ClearAll") {
    materialPath {
      moveTo(5.0f, 13.0f)
      horizontalLineToRelative(14.0f)
      verticalLineToRelative(-2.0f)
      lineTo(5.0f, 11.0f)
      verticalLineToRelative(2.0f)
      close()
      moveTo(3.0f, 17.0f)
      horizontalLineToRelative(14.0f)
      verticalLineToRelative(-2.0f)
      lineTo(3.0f, 15.0f)
      verticalLineToRelative(2.0f)
      close()
      moveTo(7.0f, 7.0f)
      verticalLineToRelative(2.0f)
      horizontalLineToRelative(14.0f)
      lineTo(21.0f, 7.0f)
      lineTo(7.0f, 7.0f)
      close()
    }
  }
}
