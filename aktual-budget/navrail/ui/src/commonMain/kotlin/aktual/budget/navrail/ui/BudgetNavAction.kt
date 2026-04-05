package aktual.budget.navrail.ui

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BudgetNavAction {
  data object SwitchFile : BudgetNavAction

  data object LogOut : BudgetNavAction

  data object Settings : BudgetNavAction

  data object About : BudgetNavAction
}
