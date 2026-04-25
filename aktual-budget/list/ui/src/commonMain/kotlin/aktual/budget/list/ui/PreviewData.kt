@file:Suppress("StringLiteralDuplication")

package aktual.budget.list.ui

import aktual.budget.model.Budget
import aktual.budget.model.BudgetId

internal val PreviewBudgetSynced =
  Budget.Synced(
    name = "Main Budget",
    hasKey = true,
    encryptKeyId = "abc123",
    cloudFileId = BudgetId("abc123"),
    groupId = "abc123",
    owner = null,
  )

internal val PreviewBudgetRemote =
  Budget.Remote(
    name = "Remote Budget",
    hasKey = false,
    encryptKeyId = null,
    cloudFileId = BudgetId("def456"),
    groupId = "def456",
    owner = null,
  )

internal val PreviewBudgetBroken =
  Budget.Broken(
    name = "Broken Budget",
    hasKey = false,
    encryptKeyId = null,
    cloudFileId = BudgetId("ghi789"),
    groupId = "ghi789",
  )
