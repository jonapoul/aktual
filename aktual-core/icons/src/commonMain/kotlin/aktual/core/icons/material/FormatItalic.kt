@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.FormatItalic: ImageVector by lazy {
  materialIcon(name = "FormatItalic", viewportSize = 960f) {
    materialPath {
      moveTo(200f, 760f)
      verticalLineToRelative(-100f)
      horizontalLineToRelative(160f)
      lineToRelative(120f, -360f)
      horizontalLineTo(320f)
      verticalLineToRelative(-100f)
      horizontalLineToRelative(400f)
      verticalLineToRelative(100f)
      horizontalLineTo(580f)
      lineTo(460f, 660f)
      horizontalLineToRelative(140f)
      verticalLineToRelative(100f)
      horizontalLineTo(200f)
      close()
    }
  }
}
