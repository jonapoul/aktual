@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.SettingsSliderAlternate: ImageVector by lazy {
  aktualIcon(name = "SettingsSliderAlternate", size = 24f) {
    aktualPath {
      moveTo(4.5f, 17.5f)
      horizontalLineToRelative(6.646f)
      arcToRelative(3.5f, 3.5f, 0f, false, false, 6.708f, 0f)
      horizontalLineTo(19.5f)
      arcToRelative(1f, 1f, 0f, false, false, 0f, -2f)
      horizontalLineToRelative(-1.646f)
      arcToRelative(3.5f, 3.5f, 0f, false, false, -6.708f, 0f)
      horizontalLineTo(4.5f)
      arcToRelative(1f, 1f, 0f, false, false, 0f, 2f)
      moveToRelative(10f, -2.5f)
      arcToRelative(1.5f, 1.5f, 0f, true, true, 0f, 3f)
      arcToRelative(1.5f, 1.5f, 0f, false, true, 0f, -3f)
      moveTo(19.5f, 6.5f)
      horizontalLineToRelative(-6.646f)
      arcToRelative(3.5f, 3.5f, 0f, false, false, -6.708f, 0f)
      horizontalLineTo(4.5f)
      arcToRelative(1f, 1f, 0f, false, false, 0f, 2f)
      horizontalLineToRelative(1.646f)
      arcToRelative(3.5f, 3.5f, 0f, false, false, 6.708f, 0f)
      horizontalLineTo(19.5f)
      arcToRelative(1f, 1f, 0f, true, false, 0f, -2f)
      moveTo(9.5f, 9f)
      arcToRelative(1.5f, 1.5f, 0f, true, true, 0f, -3f)
      arcToRelative(1.5f, 1.5f, 0f, false, true, 0f, 3f)
    }
  }
}
