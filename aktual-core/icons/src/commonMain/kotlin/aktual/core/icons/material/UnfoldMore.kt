@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.UnfoldMore: ImageVector by lazy {
  materialIcon(name = "UnfoldMore", viewportSize = 960f) {
    materialPath {
      moveTo(480f, 840f)
      lineTo(300f, 660f)
      lineToRelative(58f, -58f)
      lineToRelative(122f, 122f)
      lineToRelative(122f, -122f)
      lineToRelative(58f, 58f)
      lineToRelative(-180f, 180f)
      close()
      moveTo(358f, 362f)
      lineToRelative(-58f, -58f)
      lineToRelative(180f, -180f)
      lineToRelative(180f, 180f)
      lineToRelative(-58f, 58f)
      lineToRelative(-122f, -122f)
      lineToRelative(-122f, 122f)
      close()
    }
  }
}
