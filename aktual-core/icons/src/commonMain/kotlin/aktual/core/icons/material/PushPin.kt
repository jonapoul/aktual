@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.PushPin: ImageVector by lazy {
  materialIcon(name = "PushPin", viewportSize = 960f) {
    materialPath {
      moveTo(640f, 480f)
      lineToRelative(80f, 80f)
      verticalLineToRelative(80f)
      horizontalLineTo(520f)
      verticalLineToRelative(240f)
      lineToRelative(-40f, 40f)
      lineToRelative(-40f, -40f)
      verticalLineToRelative(-240f)
      horizontalLineTo(240f)
      verticalLineToRelative(-80f)
      lineToRelative(80f, -80f)
      verticalLineToRelative(-280f)
      horizontalLineToRelative(-40f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(400f)
      verticalLineToRelative(80f)
      horizontalLineToRelative(-40f)
      verticalLineToRelative(280f)
      close()
    }
  }
}
