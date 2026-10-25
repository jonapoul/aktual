@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.ViewHide: ImageVector by lazy {
  aktualIcon(name = "ViewHide", size = 20f) {
    aktualPath {
      moveTo(12.81f, 4.36f)
      lineToRelative(-1.77f, 1.78f)
      arcToRelative(4f, 4f, 0f, false, false, -4.9f, 4.9f)
      lineToRelative(-2.76f, 2.75f)
      curveTo(2.06f, 12.79f, 0.96f, 11.49f, 0.2f, 10f)
      arcToRelative(11f, 11f, 0f, false, true, 12.6f, -5.64f)
      close()
      moveToRelative(3.8f, 1.85f)
      curveToRelative(1.33f, 1f, 2.43f, 2.3f, 3.2f, 3.79f)
      arcToRelative(11f, 11f, 0f, false, true, -12.62f, 5.64f)
      lineToRelative(1.77f, -1.78f)
      arcToRelative(4f, 4f, 0f, false, false, 4.9f, -4.9f)
      lineToRelative(2.76f, -2.75f)
      close()
      moveToRelative(-0.25f, -3.99f)
      lineToRelative(1.42f, 1.42f)
      lineTo(3.64f, 17.78f)
      lineToRelative(-1.42f, -1.42f)
      lineTo(16.36f, 2.22f)
      close()
    }
  }
}
