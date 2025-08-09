package actual.budget.sync.ui

import actual.account.model.LoginToken
import actual.budget.model.BudgetId
import androidx.compose.runtime.Immutable

@Immutable
interface SyncBudgetNavigator {
  fun back(): Boolean
  fun toBudget(token: LoginToken, budgetId: BudgetId)
}
