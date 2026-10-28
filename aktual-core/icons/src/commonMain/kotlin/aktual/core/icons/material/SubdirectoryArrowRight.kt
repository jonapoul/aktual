@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.SubdirectoryArrowRight: ImageVector by lazy {
  materialIcon(name = "SubdirectoryArrowRight", viewportSize = 960f) {
    materialPath {
      moveTo(560f, 840f)
      lineToRelative(-57f, -57f)
      lineToRelative(144f, -143f)
      horizontalLineTo(200f)
      verticalLineToRelative(-480f)
      horizontalLineToRelative(80f)
      verticalLineToRelative(400f)
      horizontalLineToRelative(367f)
      lineTo(503f, 416f)
      lineToRelative(56f, -57f)
      lineToRelative(241f, 241f)
      lineToRelative(-240f, 240f)
      close()
    }
  }
}
