package actual.budget.sync.ui

import actual.account.model.Password
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SyncBudgetAction {
  data object Retry : SyncBudgetAction
  data object Continue : SyncBudgetAction
  data class EnterKeyPassword(val input: Password) : SyncBudgetAction
  data object ConfirmKeyPassword : SyncBudgetAction
  data object DismissPasswordDialog : SyncBudgetAction
  data object LearnMore : SyncBudgetAction
}
