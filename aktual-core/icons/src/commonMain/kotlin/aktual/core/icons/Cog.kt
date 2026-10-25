@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Cog: ImageVector by lazy {
  aktualIcon(name = "Cog", size = 20f) {
    aktualPath {
      moveTo(3.94f, 6.5f)
      lineTo(2.22f, 3.64f)
      lineToRelative(1.42f, -1.42f)
      lineTo(6.5f, 3.94f)
      curveToRelative(0.52f, -0.3f, 1.1f, -0.54f, 1.7f, -0.7f)
      lineTo(9f, 0f)
      horizontalLineToRelative(2f)
      lineToRelative(0.8f, 3.24f)
      curveToRelative(0.6f, 0.16f, 1.18f, 0.4f, 1.7f, 0.7f)
      lineToRelative(2.86f, -1.72f)
      lineToRelative(1.42f, 1.42f)
      lineToRelative(-1.72f, 2.86f)
      curveToRelative(0.3f, 0.52f, 0.54f, 1.1f, 0.7f, 1.7f)
      lineTo(20f, 9f)
      verticalLineToRelative(2f)
      lineToRelative(-3.24f, 0.8f)
      curveToRelative(-0.16f, 0.6f, -0.4f, 1.18f, -0.7f, 1.7f)
      lineToRelative(1.72f, 2.86f)
      lineToRelative(-1.42f, 1.42f)
      lineToRelative(-2.86f, -1.72f)
      curveToRelative(-0.52f, 0.3f, -1.1f, 0.54f, -1.7f, 0.7f)
      lineTo(11f, 20f)
      horizontalLineTo(9f)
      lineToRelative(-0.8f, -3.24f)
      curveToRelative(-0.6f, -0.16f, -1.18f, -0.4f, -1.7f, -0.7f)
      lineToRelative(-2.86f, 1.72f)
      lineToRelative(-1.42f, -1.42f)
      lineToRelative(1.72f, -2.86f)
      curveToRelative(-0.3f, -0.52f, -0.54f, -1.1f, -0.7f, -1.7f)
      lineTo(0f, 11f)
      verticalLineTo(9f)
      lineToRelative(3.24f, -0.8f)
      curveToRelative(0.16f, -0.6f, 0.4f, -1.18f, 0.7f, -1.7f)
      close()
      moveTo(10f, 13f)
      arcToRelative(3f, 3f, 0f, true, false, 0f, -6f)
      arcToRelative(3f, 3f, 0f, false, false, 0f, 6f)
      close()
    }
  }
}
