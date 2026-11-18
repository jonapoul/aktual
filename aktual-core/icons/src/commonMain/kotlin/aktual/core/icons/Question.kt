@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Question: ImageVector by lazy {
  aktualIcon(name = "Question", size = 20f, padded = true) {
    aktualPath {
      moveTo(10f, 20f)
      arcToRelative(10f, 10f, 0f, true, true, 0f, -20f)
      arcToRelative(10f, 10f, 0f, false, true, 0f, 20f)
      close()
      moveToRelative(2f, -13f)
      curveToRelative(0f, 0.28f, -0.21f, 0.8f, -0.42f, 1f)
      lineTo(10f, 9.58f)
      curveToRelative(-0.57f, 0.58f, -1f, 1.6f, -1f, 2.42f)
      verticalLineToRelative(1f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(-1f)
      curveToRelative(0f, -0.29f, 0.21f, -0.8f, 0.42f, -1f)
      lineTo(13f, 9.42f)
      curveToRelative(0.57f, -0.58f, 1f, -1.6f, 1f, -2.42f)
      arcToRelative(4f, 4f, 0f, true, false, -8f, 0f)
      horizontalLineToRelative(2f)
      arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
      close()
      moveToRelative(-3f, 8f)
      verticalLineToRelative(2f)
      horizontalLineToRelative(2f)
      verticalLineToRelative(-2f)
      horizontalLineTo(9f)
      close()
    }
  }
}
