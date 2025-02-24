package actual.budget.sync.ui

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SyncBudgetAction {
  data object ChangeServer : SyncBudgetAction
  data object Retry : SyncBudgetAction
}
