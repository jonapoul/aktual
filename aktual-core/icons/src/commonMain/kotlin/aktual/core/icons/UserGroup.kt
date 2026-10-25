@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.UserGroup: ImageVector by lazy {
  aktualIcon(name = "UserGroup", size = 20f) {
    aktualPath {
      moveTo(7f, 8f)
      arcToRelative(4f, 4f, 0f, true, true, 0f, -8f)
      arcToRelative(4f, 4f, 0f, false, true, 0f, 8f)
      close()
      moveToRelative(0f, 1f)
      curveToRelative(2.15f, 0f, 4.2f, 0.4f, 6.1f, 1.09f)
      lineTo(12f, 16f)
      horizontalLineToRelative(-1.25f)
      lineTo(10f, 20f)
      horizontalLineTo(4f)
      lineToRelative(-0.75f, -4f)
      horizontalLineTo(2f)
      lineTo(0.9f, 10.09f)
      arcTo(17.93f, 17.93f, 0f, false, true, 7f, 9f)
      close()
      moveToRelative(8.31f, 0.17f)
      curveToRelative(1.32f, 0.18f, 2.59f, 0.48f, 3.8f, 0.92f)
      lineTo(18f, 16f)
      horizontalLineToRelative(-1.25f)
      lineTo(16f, 20f)
      horizontalLineToRelative(-3.96f)
      lineToRelative(0.37f, -2f)
      horizontalLineToRelative(1.25f)
      lineToRelative(1.65f, -8.83f)
      close()
      moveTo(13f, 0f)
      arcToRelative(4f, 4f, 0f, true, true, -1.33f, 7.76f)
      arcToRelative(5.96f, 5.96f, 0f, false, false, 0f, -7.52f)
      curveTo(12.1f, 0.1f, 12.53f, 0f, 13f, 0f)
      close()
    }
  }
}
