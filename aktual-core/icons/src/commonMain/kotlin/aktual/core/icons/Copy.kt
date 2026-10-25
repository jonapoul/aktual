@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Copy: ImageVector by lazy {
  aktualIcon(name = "Copy", size = 20f) {
    aktualPath {
      moveTo(6f, 6f)
      verticalLineTo(2f)
      curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
      horizontalLineToRelative(10f)
      arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
      verticalLineToRelative(10f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      horizontalLineToRelative(-4f)
      verticalLineToRelative(4f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      horizontalLineTo(2f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
      verticalLineTo(8f)
      curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
      horizontalLineToRelative(4f)
      close()
      moveToRelative(2f, 0f)
      horizontalLineToRelative(4f)
      arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
      verticalLineToRelative(4f)
      horizontalLineToRelative(4f)
      verticalLineTo(2f)
      horizontalLineTo(8f)
      verticalLineToRelative(4f)
      close()
      moveTo(2f, 8f)
      verticalLineToRelative(10f)
      horizontalLineToRelative(10f)
      verticalLineTo(8f)
      horizontalLineTo(2f)
      close()
    }
  }
}
