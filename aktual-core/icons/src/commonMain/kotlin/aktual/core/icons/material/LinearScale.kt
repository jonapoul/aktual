@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.LinearScale: ImageVector by lazy {
  materialIcon(name = "LinearScale") {
    materialPath {
      moveTo(19.5f, 9.5f)
      curveToRelative(-1.03f, 0f, -1.9f, 0.62f, -2.29f, 1.5f)
      horizontalLineToRelative(-2.92f)
      curveTo(13.9f, 10.12f, 13.03f, 9.5f, 12f, 9.5f)
      reflectiveCurveToRelative(-1.9f, 0.62f, -2.29f, 1.5f)
      horizontalLineTo(6.79f)
      curveTo(6.4f, 10.12f, 5.53f, 9.5f, 4.5f, 9.5f)
      curveTo(3.12f, 9.5f, 2f, 10.62f, 2f, 12f)
      reflectiveCurveToRelative(1.12f, 2.5f, 2.5f, 2.5f)
      curveToRelative(1.03f, 0f, 1.9f, -0.62f, 2.29f, -1.5f)
      horizontalLineToRelative(2.92f)
      curveToRelative(0.39f, 0.88f, 1.26f, 1.5f, 2.29f, 1.5f)
      reflectiveCurveToRelative(1.9f, -0.62f, 2.29f, -1.5f)
      horizontalLineToRelative(2.92f)
      curveToRelative(0.39f, 0.88f, 1.26f, 1.5f, 2.29f, 1.5f)
      curveToRelative(1.38f, 0f, 2.5f, -1.12f, 2.5f, -2.5f)
      reflectiveCurveTo(20.88f, 9.5f, 19.5f, 9.5f)
      close()
    }
  }
}
