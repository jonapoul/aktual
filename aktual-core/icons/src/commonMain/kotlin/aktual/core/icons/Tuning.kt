@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Tuning: ImageVector by lazy {
  aktualIcon(name = "Tuning", size = 20f) {
    aktualPath {
      moveTo(17f, 16f)
      verticalLineToRelative(4f)
      horizontalLineToRelative(-2f)
      verticalLineToRelative(-4f)
      horizontalLineToRelative(-2f)
      verticalLineToRelative(-3f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(3f)
      horizontalLineToRelative(-2f)
      close()
      moveTo(1f, 9f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(3f)
      horizontalLineTo(1f)
      verticalLineTo(9f)
      close()
      moveToRelative(6f, -4f)
      horizontalLineToRelative(6f)
      verticalLineToRelative(3f)
      horizontalLineTo(7f)
      verticalLineTo(5f)
      close()
      moveTo(3f, 0f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(8f)
      horizontalLineTo(3f)
      verticalLineTo(0f)
      close()
      moveToRelative(12f, 0f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(12f)
      horizontalLineToRelative(-2f)
      verticalLineTo(0f)
      close()
      moveTo(9f, 0f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(4f)
      horizontalLineTo(9f)
      verticalLineTo(0f)
      close()
      moveTo(3f, 12f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(8f)
      horizontalLineTo(3f)
      verticalLineToRelative(-8f)
      close()
      moveToRelative(6f, -4f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(12f)
      horizontalLineTo(9f)
      verticalLineTo(8f)
      close()
    }
  }
}
