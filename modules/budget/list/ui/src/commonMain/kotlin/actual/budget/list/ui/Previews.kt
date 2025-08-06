package actual.budget.list.ui

import actual.budget.model.Budget
import actual.budget.model.BudgetId
import actual.budget.model.BudgetState

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
