@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.CloudUpload: ImageVector by lazy {
  aktualIcon(name = "CloudUpload", size = 20f) {
    aktualPath(strokeLineMiter = 4.0f) {
      moveTo(16.88f, 9.1f)
      arcTo(4.0f, 4.0f, 0.0f, false, true, 16.0f, 17.0f)
      horizontalLineTo(5.0f)
      arcToRelative(5.0f, 5.0f, 0.0f, false, true, -1.0f, -9.9f)
      verticalLineTo(7.0f)
      arcToRelative(3.0f, 3.0f, 0.0f, false, true, 4.52f, -2.59f)
      arcTo(4.98f, 4.98f, 0.0f, false, true, 17.0f, 8.0f)
      curveToRelative(0.0f, 0.38f, -0.04f, 0.74f, -0.12f, 1.1f)
      close()
      moveTo(11.0f, 11.0f)
      horizontalLineToRelative(3.0f)
      lineToRelative(-4.0f, -4.0f)
      lineToRelative(-4.0f, 4.0f)
      horizontalLineToRelative(3.0f)
      verticalLineToRelative(3.0f)
      horizontalLineToRelative(2.0f)
      verticalLineToRelative(-3.0f)
      close()
    }
  }
}
