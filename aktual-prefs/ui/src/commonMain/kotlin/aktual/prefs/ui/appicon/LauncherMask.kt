package aktual.prefs.ui.appicon

import aktual.prefs.components.AppIconShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape

// Shapes launchers commonly mask icons to, so people can compare the options under their own
internal enum class LauncherMask(val shape: Shape) {
  Circle(CircleShape),
  Squircle(AppIconShape),
  Square(RoundedCornerShape(percent = 12)),
  Teardrop(
    RoundedCornerShape(
      topStartPercent = 50,
      topEndPercent = 50,
      bottomEndPercent = 12,
      bottomStartPercent = 50,
    ),
  ),
}
