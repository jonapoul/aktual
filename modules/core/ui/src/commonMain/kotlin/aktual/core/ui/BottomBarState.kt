/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import aktual.core.model.PingState
import androidx.compose.runtime.Immutable

@Immutable
sealed interface BottomBarState {
  data object Hidden : BottomBarState

  data class Visible(
    val pingState: PingState,
    val budgetName: String?,
  ) : BottomBarState
}
