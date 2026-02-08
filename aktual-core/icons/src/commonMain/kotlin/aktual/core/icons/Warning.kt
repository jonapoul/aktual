@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Warning: ImageVector by lazy {
  materialIcon(name = "Warning") {
    materialPath {
      moveTo(1.0f, 21.0f)
      horizontalLineToRelative(22.0f)
      lineTo(12.0f, 2.0f)
      lineTo(1.0f, 21.0f)
      close()
      moveTo(13.0f, 18.0f)
      horizontalLineToRelative(-2.0f)
      verticalLineToRelative(-2.0f)
      horizontalLineToRelative(2.0f)
      verticalLineToRelative(2.0f)
      close()
      moveTo(13.0f, 14.0f)
      horizontalLineToRelative(-2.0f)
      verticalLineToRelative(-4.0f)
      horizontalLineToRelative(2.0f)
      verticalLineToRelative(4.0f)
      close()
    }
  }
}
