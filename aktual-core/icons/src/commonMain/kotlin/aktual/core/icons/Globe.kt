@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Globe: ImageVector by lazy {
  aktualIcon(name = "Globe", size = 20f, padded = true) {
    aktualPath {
      moveTo(10f, 20f)
      arcToRelative(10f, 10f, 0f, true, true, 0f, -20f)
      arcToRelative(10f, 10f, 0f, false, true, 0f, 20f)
      close()
      moveToRelative(2f, -2.25f)
      arcToRelative(8f, 8f, 0f, false, false, 4f, -2.46f)
      verticalLineTo(9f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
      verticalLineTo(3.07f)
      arcToRelative(7.95f, 7.95f, 0f, false, false, -3f, -1f)
      verticalLineTo(3f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      verticalLineToRelative(1f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
      verticalLineToRelative(2f)
      horizontalLineToRelative(3f)
      arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
      verticalLineToRelative(5.75f)
      close()
      moveToRelative(-4f, 0f)
      verticalLineTo(15f)
      arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
      verticalLineToRelative(-1f)
      horizontalLineToRelative(-0.5f)
      arcTo(1.5f, 1.5f, 0f, false, true, 4f, 10.5f)
      verticalLineTo(8f)
      horizontalLineTo(2.25f)
      arcTo(8.01f, 8.01f, 0f, false, false, 8f, 17.75f)
      close()
    }
  }
}
