package actual.budget.sync.ui

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SyncBudgetAction {
  data object Retry : SyncBudgetAction
  data object Continue : SyncBudgetAction
  data class EnterKeyPassword(val input: String) : SyncBudgetAction
  data object ConfirmKeyPassword : SyncBudgetAction
}
