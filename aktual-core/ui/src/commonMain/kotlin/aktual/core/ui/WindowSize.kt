@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package aktual.core.ui

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.toSize

@Composable
fun isCompactWidth(): Boolean =
  rememberWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact

@Stable
@Composable
private fun rememberWindowSizeClass(size: DpSize = rememberWindowSize()): WindowSizeClass =
  remember(size) { WindowSizeClass.calculateFromSize(size) }

@Stable
@Composable
private fun rememberWindowSize(
  density: Density = LocalDensity.current,
  windowInfo: WindowInfo = LocalWindowInfo.current,
): DpSize =
  remember(density, windowInfo) { with(density) { windowInfo.containerSize.toSize().toDpSize() } }
