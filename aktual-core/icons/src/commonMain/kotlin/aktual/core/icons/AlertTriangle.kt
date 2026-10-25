@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.AlertTriangle: ImageVector by lazy {
  aktualIcon(name = "AlertTriangle", size = 24f) {
    aktualPath {
      moveTo(23.119f, 20f)
      lineTo(13.772f, 2.15f)
      horizontalLineToRelative(0f)
      arcToRelative(2f, 2f, 0f, false, false, -3.543f, 0f)
      lineTo(0.881f, 20f)
      arcToRelative(2f, 2f, 0f, false, false, 1.772f, 2.928f)
      horizontalLineTo(21.347f)
      arcTo(2f, 2f, 0f, false, false, 23.119f, 20f)
      close()
      moveTo(11f, 8.423f)
      arcToRelative(1f, 1f, 0f, false, true, 2f, 0f)
      verticalLineToRelative(6f)
      arcToRelative(1f, 1f, 0f, true, true, -2f, 0f)
      close()
      moveToRelative(1.05f, 11.51f)
      horizontalLineToRelative(-0.028f)
      arcToRelative(1.528f, 1.528f, 0f, false, true, -1.522f, -1.47f)
      arcToRelative(1.476f, 1.476f, 0f, false, true, 1.448f, -1.53f)
      horizontalLineToRelative(0.028f)
      arcTo(1.527f, 1.527f, 0f, false, true, 13.5f, 18.4f)
      arcTo(1.475f, 1.475f, 0f, false, true, 12.05f, 19.933f)
      close()
    }
  }
}
