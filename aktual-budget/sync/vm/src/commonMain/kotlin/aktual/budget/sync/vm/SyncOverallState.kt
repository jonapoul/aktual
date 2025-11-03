/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.sync.vm

import androidx.compose.runtime.Immutable

@Immutable
enum class SyncOverallState {
  NotStarted,
  InProgress,
  Failed,
  Succeeded,
}
