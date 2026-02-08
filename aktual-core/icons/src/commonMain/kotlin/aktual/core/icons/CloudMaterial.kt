@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Cloud: ImageVector by lazy {
  materialIcon(name = "Material.Cloud") {
    materialPath {
      moveTo(19.35f, 10.04f)
      curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
      curveTo(9.11f, 4.0f, 6.6f, 5.64f, 5.35f, 8.04f)
      curveTo(2.34f, 8.36f, 0.0f, 10.91f, 0.0f, 14.0f)
      curveToRelative(0.0f, 3.31f, 2.69f, 6.0f, 6.0f, 6.0f)
      horizontalLineToRelative(13.0f)
      curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
      curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
      close()
    }
  }
}
