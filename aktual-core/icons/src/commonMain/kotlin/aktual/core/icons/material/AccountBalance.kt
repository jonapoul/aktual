@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.AccountBalance: ImageVector by lazy {
  materialIcon(name = "AccountBalance", viewportSize = 960f) {
    materialPath {
      moveTo(200f, 680f)
      verticalLineToRelative(-280f)
      horizontalLineToRelative(80f)
      verticalLineToRelative(280f)
      horizontalLineToRelative(-80f)
      close()
      moveToRelative(240f, 0f)
      verticalLineToRelative(-280f)
      horizontalLineToRelative(80f)
      verticalLineToRelative(280f)
      horizontalLineToRelative(-80f)
      close()
      moveTo(80f, 840f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(800f)
      verticalLineToRelative(80f)
      horizontalLineTo(80f)
      close()
      moveToRelative(600f, -160f)
      verticalLineToRelative(-280f)
      horizontalLineToRelative(80f)
      verticalLineToRelative(280f)
      horizontalLineToRelative(-80f)
      close()
      moveTo(80f, 320f)
      verticalLineToRelative(-80f)
      lineToRelative(400f, -200f)
      lineToRelative(400f, 200f)
      verticalLineToRelative(80f)
      horizontalLineTo(80f)
      close()
    }
  }
}
