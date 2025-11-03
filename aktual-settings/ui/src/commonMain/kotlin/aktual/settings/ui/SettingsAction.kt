/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.settings.ui

import aktual.settings.vm.PreferenceValue
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SettingsAction {
  data object NavBack : SettingsAction
  data class PreferenceChange(val value: PreferenceValue) : SettingsAction
}
