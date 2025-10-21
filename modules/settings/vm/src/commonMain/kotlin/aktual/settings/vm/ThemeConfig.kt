/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.settings.vm

import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import androidx.compose.runtime.Immutable

@Immutable
data class ThemeConfig(
  val regular: RegularColorSchemeType,
  val dark: DarkColorSchemeType,
)
