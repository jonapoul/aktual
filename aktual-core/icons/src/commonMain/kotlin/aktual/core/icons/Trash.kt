@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Trash: ImageVector by lazy {
  aktualIcon(name = "Trash", size = 20f) {
    aktualPath {
      moveTo(6f, 2f)
      lineToRelative(2f, -2f)
      horizontalLineToRelative(4f)
      lineToRelative(2f, 2f)
      horizontalLineToRelative(4f)
      verticalLineToRelative(2f)
      horizontalLineTo(2f)
      verticalLineTo(2f)
      horizontalLineToRelative(4f)
      close()
      moveTo(3f, 6f)
      horizontalLineToRelative(14f)
      lineToRelative(-1f, 14f)
      horizontalLineTo(4f)
      lineTo(3f, 6f)
      close()
      moveToRelative(5f, 2f)
      verticalLineToRelative(10f)
      horizontalLineToRelative(1f)
      verticalLineTo(8f)
      horizontalLineTo(8f)
      close()
      moveToRelative(3f, 0f)
      verticalLineToRelative(10f)
      horizontalLineToRelative(1f)
      verticalLineTo(8f)
      horizontalLineToRelative(-1f)
      close()
    }
  }
}
