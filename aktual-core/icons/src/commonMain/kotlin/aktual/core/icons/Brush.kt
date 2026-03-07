@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Brush: ImageVector by lazy {
  materialIcon(name = "Brush") {
    materialPath {
      moveTo(7f, 14f)
      curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
      curveToRelative(0f, 1.31f, -1.16f, 2f, -2f, 2f)
      curveToRelative(0.92f, 1.22f, 2.49f, 2f, 4f, 2f)
      curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f)
      curveToRelative(0f, -1.66f, -1.34f, -3f, -3f, -3f)
      close()
      moveTo(20.71f, 4.63f)
      lineToRelative(-1.34f, -1.34f)
      curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
      lineTo(9f, 12.25f)
      lineTo(11.75f, 15f)
      lineToRelative(8.96f, -8.96f)
      curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
      close()
    }
  }
}
