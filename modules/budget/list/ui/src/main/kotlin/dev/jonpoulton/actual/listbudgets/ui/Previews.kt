package dev.jonpoulton.actual.listbudgets.ui

import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.listbudgets.vm.Budget
import dev.jonpoulton.actual.listbudgets.vm.BudgetState

internal val PreviewBudget = Budget(
  name = "Main Budget",
  state = BudgetState.Synced,
  hasKey = true,
  encryptKeyId = "abc123",
  groupId = "abc123",
  cloudFileId = "abc123",
)

internal val PreviewVersions = ActualVersions(app = "1.2.3", server = "24.3.0")
