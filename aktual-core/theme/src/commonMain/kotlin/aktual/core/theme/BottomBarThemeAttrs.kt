package aktual.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class BottomBarThemeAttrs(
  val shouldHazeOnRootLevel: Boolean,
  val background: Colors.() -> Color,
  val foreground: Colors.() -> Color,
)
