@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.ColorPalette: ImageVector by lazy {
  aktualIcon(name = "ColorPalette", size = 20f, padded = true) {
    aktualPath {
      moveTo(9f, 20f)
      verticalLineToRelative(-1.7f)
      lineToRelative(0.01f, -0.24f)
      lineTo(15.07f, 12f)
      horizontalLineToRelative(2.94f)
      curveToRelative(1.1f, 0f, 1.99f, 0.89f, 1.99f, 2f)
      verticalLineToRelative(4f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      horizontalLineTo(9f)
      close()
      moveToRelative(0f, -3.34f)
      verticalLineTo(5.34f)
      lineToRelative(2.08f, -2.07f)
      arcToRelative(1.99f, 1.99f, 0f, false, true, 2.82f, 0f)
      lineToRelative(2.83f, 2.83f)
      arcToRelative(2f, 2f, 0f, false, true, 0f, 2.82f)
      lineTo(9f, 16.66f)
      close()
      moveTo(0f, 1.99f)
      curveTo(0f, 0.9f, 0.89f, 0f, 2f, 0f)
      horizontalLineToRelative(4f)
      arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
      verticalLineToRelative(16f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      horizontalLineTo(2f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
      verticalLineTo(2f)
      close()
      moveTo(4f, 17f)
      arcToRelative(1f, 1f, 0f, true, false, 0f, -2f)
      arcToRelative(1f, 1f, 0f, false, false, 0f, 2f)
      close()
    }
  }
}
