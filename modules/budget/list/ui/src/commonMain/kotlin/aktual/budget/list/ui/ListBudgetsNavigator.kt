/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.list.ui

import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ListBudgetsNavigator {
  fun toAbout()
  fun toChangePassword()
  fun toUrl()
  fun toSettings()
  fun toSyncBudget(token: LoginToken, id: BudgetId)
}
