/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.list.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DeletingState {
  data object Inactive : DeletingState

  data class Active(
    val deletingLocal: Boolean = false,
    val deletingRemote: Boolean = false,
  ) : DeletingState
}
