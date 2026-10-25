@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.LightBulb: ImageVector by lazy {
  aktualIcon(name = "LightBulb", size = 20f) {
    aktualPath {
      moveTo(7f, 13.33f)
      arcToRelative(7f, 7f, 0f, true, true, 6f, 0f)
      verticalLineTo(16f)
      horizontalLineTo(7f)
      verticalLineToRelative(-2.67f)
      close()
      moveTo(7f, 17f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(1.5f)
      curveToRelative(0f, 0.83f, -0.67f, 1.5f, -1.5f, 1.5f)
      horizontalLineToRelative(-3f)
      arcTo(1.5f, 1.5f, 0f, false, true, 7f, 18.5f)
      verticalLineTo(17f)
      close()
      moveToRelative(2f, -5.1f)
      verticalLineTo(14f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(-2.1f)
      arcToRelative(5f, 5f, 0f, true, false, -2f, 0f)
      close()
    }
  }
}
