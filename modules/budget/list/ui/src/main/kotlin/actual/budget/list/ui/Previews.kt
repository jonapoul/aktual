package actual.budget.list.ui

import actual.budget.list.vm.Budget
import actual.budget.list.vm.BudgetState
import actual.core.model.ActualVersions

internal val PreviewBudget = Budget(
  name = "Main Budget",
  state = BudgetState.Synced,
  hasKey = true,
  encryptKeyId = "abc123",
  groupId = "abc123",
  cloudFileId = "abc123",
)

internal val PreviewVersions = ActualVersions(app = "1.2.3", server = "24.3.0")
