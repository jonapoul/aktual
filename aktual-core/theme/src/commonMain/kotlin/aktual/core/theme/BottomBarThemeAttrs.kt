package aktual.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class BottomBarThemeAttrs(
  val shouldBlurOnRootLevel: Boolean,
  val background: Theme.() -> Color,
  val foreground: Theme.() -> Color,
)
