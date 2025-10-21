/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.sync.ui

import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface SyncBudgetNavigator {
  fun back(): Boolean
  fun toBudget(token: LoginToken, budgetId: BudgetId)
}
