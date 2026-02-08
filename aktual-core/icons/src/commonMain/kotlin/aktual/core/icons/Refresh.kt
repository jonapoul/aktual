@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Refresh: ImageVector by lazy {
  materialIcon(name = "Material.Refresh") {
    materialPath {
      moveTo(17.65f, 6.35f)
      curveTo(16.2f, 4.9f, 14.21f, 4.0f, 12.0f, 4.0f)
      curveToRelative(-4.42f, 0.0f, -7.99f, 3.58f, -7.99f, 8.0f)
      reflectiveCurveToRelative(3.57f, 8.0f, 7.99f, 8.0f)
      curveToRelative(3.73f, 0.0f, 6.84f, -2.55f, 7.73f, -6.0f)
      horizontalLineToRelative(-2.08f)
      curveToRelative(-0.82f, 2.33f, -3.04f, 4.0f, -5.65f, 4.0f)
      curveToRelative(-3.31f, 0.0f, -6.0f, -2.69f, -6.0f, -6.0f)
      reflectiveCurveToRelative(2.69f, -6.0f, 6.0f, -6.0f)
      curveToRelative(1.66f, 0.0f, 3.14f, 0.69f, 4.22f, 1.78f)
      lineTo(13.0f, 11.0f)
      horizontalLineToRelative(7.0f)
      verticalLineTo(4.0f)
      lineToRelative(-2.35f, 2.35f)
      close()
    }
  }
}
