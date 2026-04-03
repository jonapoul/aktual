@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Reports: ImageVector by lazy {
  aktualIcon(name = "Reports", size = 20f) {
    aktualPath {
      moveTo(19f, 18f)
      horizontalLineToRelative(-1f)
      verticalLineToRelative(-8f)
      curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
      curveToRelative(-0.6f, 0f, -1f, 0.4f, -1f, 1f)
      verticalLineToRelative(8f)
      horizontalLineToRelative(-3f)
      verticalLineTo(1f)
      curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
      curveToRelative(-0.6f, 0f, -1f, 0.4f, -1f, 1f)
      verticalLineToRelative(17f)
      horizontalLineTo(8f)
      verticalLineTo(7f)
      curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
      curveTo(6.4f, 6f, 6f, 6.4f, 6f, 7f)
      verticalLineToRelative(11f)
      horizontalLineTo(3f)
      verticalLineTo(3f)
      curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
      curveTo(1.4f, 2f, 1f, 2.4f, 1f, 3f)
      verticalLineToRelative(15f)
      curveToRelative(-0.6f, 0f, -1f, 0.4f, -1f, 1f)
      curveToRelative(0f, 0.6f, 0.4f, 1f, 1f, 1f)
      horizontalLineToRelative(18f)
      curveToRelative(0.6f, 0f, 1f, -0.4f, 1f, -1f)
      curveTo(20f, 18.4f, 19.6f, 18f, 19f, 18f)
      close()
    }
  }
}
