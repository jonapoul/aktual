package aktual.budget.budgeting.ui

import aktual.budget.model.CategoryGroupId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BudgetAction

internal data object Refresh : BudgetAction

internal data object Retry : BudgetAction

internal data object ToggleSpent : BudgetAction

internal data object ToggleHidden : BudgetAction

internal data class ToggleGroup(val id: CategoryGroupId) : BudgetAction

internal data object ReviewUncategorised : BudgetAction

@Immutable
internal fun interface BudgetActionHandler {
  operator fun invoke(action: BudgetAction)
}
