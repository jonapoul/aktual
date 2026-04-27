@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Logout: ImageVector by lazy {
  materialIcon(name = "Logout", autoMirror = true) {
    materialPath {
      moveTo(17f, 7f)
      lineToRelative(-1.41f, 1.41f)
      lineTo(18.17f, 11f)
      horizontalLineTo(8f)
      verticalLineToRelative(2f)
      horizontalLineToRelative(10.17f)
      lineToRelative(-2.58f, 2.58f)
      lineTo(17f, 17f)
      lineToRelative(5f, -5f)
      close()
      moveTo(4f, 5f)
      horizontalLineToRelative(8f)
      verticalLineTo(3f)
      horizontalLineTo(4f)
      curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
      verticalLineToRelative(14f)
      curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
      horizontalLineToRelative(8f)
      verticalLineToRelative(-2f)
      horizontalLineTo(4f)
      verticalLineTo(5f)
      close()
    }
  }
}
