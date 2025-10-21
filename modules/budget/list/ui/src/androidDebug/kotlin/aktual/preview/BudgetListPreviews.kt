/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.model.Budget
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetState

internal val PreviewBudgetSynced = Budget(
  name = "Main Budget",
  state = BudgetState.Synced,
  encryptKeyId = "abc123",
  groupId = "abc123",
  cloudFileId = BudgetId("abc123"),
  hasKey = true,
)

internal val PreviewBudgetSyncing = PreviewBudgetSynced.copy(
  name = "Syncing Budget",
  state = BudgetState.Syncing,
  encryptKeyId = null,
)

internal val PreviewBudgetBroken = PreviewBudgetSynced.copy(
  name = "Broken Budget",
  state = BudgetState.Broken,
  encryptKeyId = null,
)
