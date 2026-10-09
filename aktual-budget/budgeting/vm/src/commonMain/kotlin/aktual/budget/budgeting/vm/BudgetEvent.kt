package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.UndoToken

sealed interface BudgetEvent {
  // A change was written, which the token undoes
  data class Updated(val token: UndoToken) : BudgetEvent
}
