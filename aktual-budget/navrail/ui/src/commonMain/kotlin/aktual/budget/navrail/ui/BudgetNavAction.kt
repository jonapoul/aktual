package aktual.budget.navrail.ui

import androidx.compose.runtime.Immutable

// Everything the budget menus can do. BudgetNavAction is the part handled outside the budget
@Immutable internal sealed interface BudgetMenuAction

internal data object RenameBudget : BudgetMenuAction

internal data object OpenApiKeys : BudgetMenuAction

@Immutable internal sealed interface BudgetNavAction : BudgetMenuAction

internal data object SwitchFile : BudgetNavAction

internal data object LogOut : BudgetNavAction

internal data object ExitDemo : BudgetNavAction

internal data object Settings : BudgetNavAction

internal data object About : BudgetNavAction
