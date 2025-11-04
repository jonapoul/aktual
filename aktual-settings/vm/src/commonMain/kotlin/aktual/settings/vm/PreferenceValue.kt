/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.settings.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PreferenceValue {
  data class Theme(val config: ThemeConfig) : PreferenceValue
  data class ShowBottomBar(val show: Boolean) : PreferenceValue
}
