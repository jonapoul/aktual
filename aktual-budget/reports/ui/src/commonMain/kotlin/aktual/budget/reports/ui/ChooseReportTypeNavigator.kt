/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui

import aktual.budget.model.BudgetId
import aktual.budget.model.WidgetId
import aktual.core.model.LoginToken
import androidx.compose.runtime.Immutable

@Immutable
interface ChooseReportTypeNavigator {
  fun back(): Boolean
  fun toReport(token: LoginToken, budgetId: BudgetId, widgetId: WidgetId)
}
