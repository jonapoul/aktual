package aktual.budget.list.vm

sealed interface ListBudgetsEvent {
  data object NavToBudget : ListBudgetsEvent

  data object LogOut : ListBudgetsEvent
}
