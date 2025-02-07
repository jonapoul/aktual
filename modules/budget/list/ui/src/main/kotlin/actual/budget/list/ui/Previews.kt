package actual.budget.list.ui

import actual.budget.list.vm.Budget
import actual.budget.list.vm.BudgetState
import actual.core.versions.ActualVersions

internal val PreviewBudgetSynced = Budget(
  name = "Main Budget",
  state = BudgetState.Synced,
  hasKey = true,
  encryptKeyId = "abc123",
  groupId = "abc123",
  cloudFileId = "abc123",
)

internal val PreviewBudgetSyncing = PreviewBudgetSynced.copy(
  name = "Syncing Budget",
  state = BudgetState.Syncing,
  hasKey = false,
  encryptKeyId = null,
)

internal val PreviewBudgetBroken = PreviewBudgetSynced.copy(
  name = "Broken Budget",
  state = BudgetState.Broken,
  hasKey = false,
  encryptKeyId = null,
)

internal val PreviewVersions = ActualVersions(app = "1.2.3", server = "24.3.0")
