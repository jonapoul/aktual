@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Pin: ImageVector by lazy {
  aktualIcon(name = "Pin", size = 20f) {
    aktualPath {
      moveTo(11f, 12f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(-1f)
      lineToRelative(-3f, -1f)
      verticalLineTo(2f)
      lineToRelative(3f, -1f)
      verticalLineTo(0f)
      horizontalLineTo(3f)
      verticalLineToRelative(1f)
      lineToRelative(3f, 1f)
      verticalLineToRelative(8f)
      lineToRelative(-3f, 1f)
      verticalLineToRelative(1f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(7f)
      lineToRelative(1f, 1f)
      lineToRelative(1f, -1f)
      verticalLineToRelative(-7f)
      close()
    }
  }
}
