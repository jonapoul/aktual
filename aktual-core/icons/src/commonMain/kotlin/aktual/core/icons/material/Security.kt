@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Security: ImageVector by lazy {
  materialIcon(name = "Security") {
    materialPath {
      moveTo(12.0f, 1.0f)
      lineTo(3.0f, 5.0f)
      verticalLineToRelative(6.0f)
      curveToRelative(0.0f, 5.55f, 3.84f, 10.74f, 9.0f, 12.0f)
      curveToRelative(5.16f, -1.26f, 9.0f, -6.45f, 9.0f, -12.0f)
      lineTo(21.0f, 5.0f)
      lineToRelative(-9.0f, -4.0f)
      close()
      moveTo(12.0f, 11.99f)
      horizontalLineToRelative(7.0f)
      curveToRelative(-0.53f, 4.12f, -3.28f, 7.79f, -7.0f, 8.94f)
      lineTo(12.0f, 12.0f)
      lineTo(5.0f, 12.0f)
      lineTo(5.0f, 6.3f)
      lineToRelative(7.0f, -3.11f)
      verticalLineToRelative(8.8f)
      close()
    }
  }
}
