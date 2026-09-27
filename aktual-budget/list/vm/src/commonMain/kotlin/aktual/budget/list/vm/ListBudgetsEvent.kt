package aktual.budget.list.vm

import aktual.budget.model.BudgetId

sealed interface ListBudgetsEvent {
  data object NavToBudget : ListBudgetsEvent

  data object LogOut : ListBudgetsEvent

  data class ShowSyncDialog(val id: BudgetId) : ListBudgetsEvent
}
