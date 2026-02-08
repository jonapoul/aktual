@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Info: ImageVector by lazy {
  materialIcon(name = "Material.Info") {
    materialPath {
      moveTo(12.0f, 2.0f)
      curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
      reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
      reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
      reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
      close()
      moveTo(13.0f, 17.0f)
      horizontalLineToRelative(-2.0f)
      verticalLineToRelative(-6.0f)
      horizontalLineToRelative(2.0f)
      verticalLineToRelative(6.0f)
      close()
      moveTo(13.0f, 9.0f)
      horizontalLineToRelative(-2.0f)
      lineTo(11.0f, 7.0f)
      horizontalLineToRelative(2.0f)
      verticalLineToRelative(2.0f)
      close()
    }
  }
}
