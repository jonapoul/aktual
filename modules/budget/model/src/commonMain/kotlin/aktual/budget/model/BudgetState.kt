/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class BudgetState {
  Local,
  Remote,
  Synced,
  Syncing,
  Detached,
  Broken,
  Unknown,
}
