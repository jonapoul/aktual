@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.MoreVert: ImageVector by lazy {
  materialIcon(name = "MoreVert") {
    materialPath {
      moveTo(12.0f, 8.0f)
      curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
      reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
      reflectiveCurveToRelative(-2.0f, 0.9f, -2.0f, 2.0f)
      reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
      close()
      moveTo(12.0f, 10.0f)
      curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
      reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
      reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
      reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
      close()
      moveTo(12.0f, 16.0f)
      curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
      reflectiveCurveToRelative(0.9f, 2.0f, 2.0f, 2.0f)
      reflectiveCurveToRelative(2.0f, -0.9f, 2.0f, -2.0f)
      reflectiveCurveToRelative(-0.9f, -2.0f, -2.0f, -2.0f)
      close()
    }
  }
}
