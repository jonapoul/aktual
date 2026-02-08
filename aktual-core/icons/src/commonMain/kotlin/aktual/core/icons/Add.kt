@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Add: ImageVector by lazy {
  materialIcon(name = "Material.Add") {
    materialPath {
      moveTo(19.0f, 13.0f)
      horizontalLineToRelative(-6.0f)
      verticalLineToRelative(6.0f)
      horizontalLineToRelative(-2.0f)
      verticalLineToRelative(-6.0f)
      horizontalLineTo(5.0f)
      verticalLineToRelative(-2.0f)
      horizontalLineToRelative(6.0f)
      verticalLineTo(5.0f)
      horizontalLineToRelative(2.0f)
      verticalLineToRelative(6.0f)
      horizontalLineToRelative(6.0f)
      verticalLineToRelative(2.0f)
      close()
    }
  }
}
