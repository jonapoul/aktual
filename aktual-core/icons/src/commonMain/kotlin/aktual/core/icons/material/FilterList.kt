@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.FilterList: ImageVector by lazy {
  materialIcon(name = "FilterList", viewportSize = 960f) {
    materialPath {
      moveTo(400f, 720f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(160f)
      verticalLineToRelative(80f)
      horizontalLineTo(400f)
      close()
      moveTo(240f, 520f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(480f)
      verticalLineToRelative(80f)
      horizontalLineTo(240f)
      close()
      moveTo(120f, 320f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(720f)
      verticalLineToRelative(80f)
      horizontalLineTo(120f)
      close()
    }
  }
}
