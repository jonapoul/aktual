@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Merge: ImageVector by lazy {
  materialIcon(name = "Merge", viewportSize = 960f) {
    materialPath {
      moveTo(256f, 840f)
      lineToRelative(-56f, -56f)
      lineToRelative(193f, -194f)
      quadToRelative(23f, -23f, 35f, -52f)
      reflectiveQuadToRelative(12f, -61f)
      verticalLineToRelative(-204f)
      lineToRelative(-64f, 63f)
      lineToRelative(-56f, -56f)
      lineToRelative(160f, -160f)
      lineToRelative(160f, 160f)
      lineToRelative(-56f, 56f)
      lineToRelative(-64f, -63f)
      verticalLineToRelative(204f)
      quadToRelative(0f, 32f, 12f, 61f)
      reflectiveQuadToRelative(35f, 52f)
      lineToRelative(193f, 194f)
      lineToRelative(-56f, 56f)
      lineToRelative(-224f, -224f)
      lineToRelative(-224f, 224f)
      close()
    }
  }
}
