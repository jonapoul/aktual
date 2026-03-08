package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

@Composable
actual fun isMobile(): Boolean {
  val windowInfo = LocalWindowInfo.current
  val density = LocalDensity.current
  return remember(windowInfo, density) {
    with(density) { windowInfo.containerSize.width.toDp() } < 600.dp
  }
}
