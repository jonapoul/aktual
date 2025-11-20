/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

// No-op on desktop
@Stable
@Composable
actual fun SetStatusBarColors(
  theme: Theme,
  statusBarColor: Color,
  navigationBarColor: Color,
) = Unit
