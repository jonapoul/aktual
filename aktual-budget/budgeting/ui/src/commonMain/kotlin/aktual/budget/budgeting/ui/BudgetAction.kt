package aktual.budget.budgeting.ui

import aktual.budget.model.CategoryGroupId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth

@Immutable internal sealed interface BudgetAction

internal data object Refresh : BudgetAction

internal data object Retry : BudgetAction

internal data class ShowMonth(val month: YearMonth) : BudgetAction

internal data object PreviousMonth : BudgetAction

internal data object NextMonth : BudgetAction

internal data object ShowToday : BudgetAction

internal data class SetMonthCount(val count: Int) : BudgetAction

internal data class SetFittingMonths(val count: Int) : BudgetAction

internal data object ToggleSpent : BudgetAction

internal data object ToggleHidden : BudgetAction

internal data class ToggleGroup(val id: CategoryGroupId) : BudgetAction

internal data object ReviewUncategorised : BudgetAction

@Immutable
internal fun interface BudgetActionHandler {
  operator fun invoke(action: BudgetAction)
}
