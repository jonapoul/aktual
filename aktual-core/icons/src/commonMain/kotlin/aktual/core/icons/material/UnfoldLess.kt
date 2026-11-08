@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.UnfoldLess: ImageVector by lazy {
  materialIcon(name = "UnfoldLess", viewportSize = 960f) {
    materialPath {
      moveTo(356f, 800f)
      lineToRelative(-56f, -56f)
      lineToRelative(180f, -180f)
      lineToRelative(180f, 180f)
      lineToRelative(-56f, 56f)
      lineToRelative(-124f, -124f)
      lineToRelative(-124f, 124f)
      close()
      moveToRelative(124f, -404f)
      lineTo(300f, 216f)
      lineToRelative(56f, -56f)
      lineToRelative(124f, 124f)
      lineToRelative(124f, -124f)
      lineToRelative(56f, 56f)
      lineToRelative(-180f, 180f)
      close()
    }
  }
}
