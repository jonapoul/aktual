@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.SwapHoriz: ImageVector by lazy {
  materialIcon(name = "SwapHoriz") {
    materialPath {
      moveTo(6.99f, 11.0f)
      lineTo(3.0f, 15.0f)
      lineToRelative(3.99f, 4.0f)
      verticalLineToRelative(-3.0f)
      horizontalLineTo(14.0f)
      verticalLineToRelative(-2.0f)
      horizontalLineTo(6.99f)
      verticalLineToRelative(-3.0f)
      close()
      moveTo(21.0f, 9.0f)
      lineToRelative(-3.99f, -4.0f)
      verticalLineToRelative(3.0f)
      horizontalLineTo(10.0f)
      verticalLineToRelative(2.0f)
      horizontalLineToRelative(7.01f)
      verticalLineToRelative(3.0f)
      lineTo(21.0f, 9.0f)
      close()
    }
  }
}
