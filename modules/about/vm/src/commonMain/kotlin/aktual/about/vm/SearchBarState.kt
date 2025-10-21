/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.about.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SearchBarState {
  data class Visible(val text: String) : SearchBarState
  data object Gone : SearchBarState
}
