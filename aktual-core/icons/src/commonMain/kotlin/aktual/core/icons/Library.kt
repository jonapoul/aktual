@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Library: ImageVector by lazy {
  aktualIcon(name = "Library", size = 20f) {
    aktualPath {
      moveTo(0f, 6f)
      lineToRelative(10f, -6f)
      lineToRelative(10f, 6f)
      verticalLineToRelative(2f)
      horizontalLineTo(0f)
      verticalLineTo(6f)
      close()
      moveToRelative(0f, 12f)
      horizontalLineToRelative(20f)
      verticalLineToRelative(2f)
      horizontalLineTo(0f)
      verticalLineToRelative(-2f)
      close()
      moveToRelative(2f, -2f)
      horizontalLineToRelative(16f)
      verticalLineToRelative(2f)
      horizontalLineTo(2f)
      verticalLineToRelative(-2f)
      close()
      moveToRelative(0f, -8f)
      horizontalLineToRelative(4f)
      verticalLineToRelative(8f)
      horizontalLineTo(2f)
      verticalLineTo(8f)
      close()
      moveToRelative(6f, 0f)
      horizontalLineToRelative(4f)
      verticalLineToRelative(8f)
      horizontalLineTo(8f)
      verticalLineTo(8f)
      close()
      moveToRelative(6f, 0f)
      horizontalLineToRelative(4f)
      verticalLineToRelative(8f)
      horizontalLineToRelative(-4f)
      verticalLineTo(8f)
      close()
    }
  }
}
