package actual.budget.list.ui

import actual.budget.list.vm.Budget
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ListBudgetsAction {
  data object Reload : ListBudgetsAction
  data object ChangePassword : ListBudgetsAction
  data object ChangeServer : ListBudgetsAction
  data object OpenInBrowser : ListBudgetsAction
  data class Open(val budget: Budget) : ListBudgetsAction
  data class Delete(val budget: Budget) : ListBudgetsAction
}
