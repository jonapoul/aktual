package actual.budget.list.ui

import actual.budget.list.vm.Budget
import actual.budget.list.vm.BudgetState
import actual.budget.model.BudgetId
import actual.core.versions.ActualVersions

internal val PreviewBudgetSynced = Budget(
  name = "Main Budget",
  state = BudgetState.Synced,
  encryptKeyId = "abc123",
  groupId = "abc123",
  cloudFileId = BudgetId("abc123"),
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

internal val PreviewVersions = ActualVersions.Dummy
