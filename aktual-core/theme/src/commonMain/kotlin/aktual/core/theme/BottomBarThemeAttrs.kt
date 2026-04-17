package aktual.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
fun interface BottomBarThemeAttrs {
  operator fun invoke(theme: Theme): Color
}
