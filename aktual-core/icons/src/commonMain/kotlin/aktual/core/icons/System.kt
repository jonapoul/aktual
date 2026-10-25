@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.System: ImageVector by lazy {
  aktualIcon(name = "System", size = 24f) {
    aktualPath {
      moveTo(4f, 16f)
      horizontalLineTo(20f)
      verticalLineTo(5f)
      horizontalLineTo(4f)
      verticalLineTo(16f)
      close()
      moveTo(13f, 18f)
      verticalLineTo(20f)
      horizontalLineTo(17f)
      verticalLineTo(22f)
      horizontalLineTo(7f)
      verticalLineTo(20f)
      horizontalLineTo(11f)
      verticalLineTo(18f)
      horizontalLineTo(2.9918f)
      curveTo(2.44405f, 18f, 2f, 17.5511f, 2f, 16.9925f)
      verticalLineTo(4.00748f)
      curveTo(2f, 3.45107f, 2.45531f, 3f, 2.9918f, 3f)
      horizontalLineTo(21.0082f)
      curveTo(21.556f, 3f, 22f, 3.44892f, 22f, 4.00748f)
      verticalLineTo(16.9925f)
      curveTo(22f, 17.5489f, 21.5447f, 18f, 21.0082f, 18f)
      horizontalLineTo(13f)
      close()
    }
  }
}
