@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Brightness2: ImageVector by lazy {
  materialIcon(name = "Brightness2") {
    materialPath {
      moveTo(10.0f, 2.0f)
      curveToRelative(-1.82f, 0.0f, -3.53f, 0.5f, -5.0f, 1.35f)
      curveTo(7.99f, 5.08f, 10.0f, 8.3f, 10.0f, 12.0f)
      reflectiveCurveToRelative(-2.01f, 6.92f, -5.0f, 8.65f)
      curveTo(6.47f, 21.5f, 8.18f, 22.0f, 10.0f, 22.0f)
      curveToRelative(5.52f, 0.0f, 10.0f, -4.48f, 10.0f, -10.0f)
      reflectiveCurveTo(15.52f, 2.0f, 10.0f, 2.0f)
      close()
    }
  }
}
