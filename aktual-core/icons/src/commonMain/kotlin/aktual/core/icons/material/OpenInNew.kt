@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.OpenInNew: ImageVector by lazy {
  materialIcon(name = "OpenInNew", autoMirror = true) {
    materialPath {
      moveTo(19f, 19f)
      horizontalLineTo(5f)
      verticalLineTo(5f)
      horizontalLineToRelative(7f)
      verticalLineTo(3f)
      horizontalLineTo(5f)
      curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f)
      verticalLineToRelative(14f)
      curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
      horizontalLineToRelative(14f)
      curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
      verticalLineToRelative(-7f)
      horizontalLineToRelative(-2f)
      verticalLineToRelative(7f)
      close()
      moveTo(14f, 3f)
      verticalLineToRelative(2f)
      horizontalLineToRelative(3.59f)
      lineToRelative(-9.83f, 9.83f)
      lineToRelative(1.41f, 1.41f)
      lineTo(19f, 6.41f)
      verticalLineTo(10f)
      horizontalLineToRelative(2f)
      verticalLineTo(3f)
      horizontalLineToRelative(-7f)
      close()
    }
  }
}
