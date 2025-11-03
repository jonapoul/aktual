/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.settings.ui

import androidx.compose.runtime.Immutable

@Immutable
fun interface SettingsNavigator {
  fun back(): Boolean
}
