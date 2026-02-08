@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.BarChart: ImageVector by lazy {
  materialIcon(name = "BarChart") {
    materialPath {
      moveTo(4.0f, 9.0f)
      horizontalLineToRelative(4.0f)
      verticalLineToRelative(11.0f)
      horizontalLineToRelative(-4.0f)
      close()
    }
    materialPath {
      moveTo(16.0f, 13.0f)
      horizontalLineToRelative(4.0f)
      verticalLineToRelative(7.0f)
      horizontalLineToRelative(-4.0f)
      close()
    }
    materialPath {
      moveTo(10.0f, 4.0f)
      horizontalLineToRelative(4.0f)
      verticalLineToRelative(16.0f)
      horizontalLineToRelative(-4.0f)
      close()
    }
  }
}
