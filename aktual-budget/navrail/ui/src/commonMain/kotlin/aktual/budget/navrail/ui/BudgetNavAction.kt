package aktual.budget.navrail.ui

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BudgetNavAction

internal data object SwitchFile : BudgetNavAction

internal data object LogOut : BudgetNavAction

internal data object Settings : BudgetNavAction

internal data object About : BudgetNavAction

@Immutable
internal fun interface BudgetNavActionHandler {
  operator fun invoke(action: BudgetNavAction)
}
