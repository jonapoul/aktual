@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.InformationOutline: ImageVector by lazy {
  aktualIcon(name = "InformationOutline", size = 20f) {
    aktualPath {
      moveTo(2.93f, 17.07f)
      arcTo(10f, 10f, 0f, true, true, 17.07f, 2.93f)
      arcTo(10f, 10f, 0f, false, true, 2.93f, 17.07f)
      close()
      moveToRelative(12.73f, -1.41f)
      arcTo(8f, 8f, 0f, true, false, 4.34f, 4.34f)
      arcToRelative(8f, 8f, 0f, false, false, 11.32f, 11.32f)
      close()
      moveTo(9f, 11f)
      verticalLineTo(9f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(6f)
      horizontalLineTo(9f)
      verticalLineToRelative(-4f)
      close()
      moveToRelative(0f, -6f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(2f)
      horizontalLineTo(9f)
      verticalLineTo(5f)
      close()
    }
  }
}
