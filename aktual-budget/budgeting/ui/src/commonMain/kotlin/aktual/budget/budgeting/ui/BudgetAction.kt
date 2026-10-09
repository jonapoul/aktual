package aktual.budget.budgeting.ui

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
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

internal data class OpenCategory(val month: YearMonth, val category: CategoryId) : BudgetAction

// Opens the budget sheet on compact widths. Expanded widths edit in place
internal data class EditBudget(val month: YearMonth, val category: CategoryId) : BudgetAction

internal data class SetBudget(val month: YearMonth, val category: CategoryId, val input: String) :
  BudgetAction

internal data class ApplyQuickAction(
  val month: YearMonth,
  val category: CategoryId,
  val action: QuickAction,
) : BudgetAction

@Immutable
internal fun interface BudgetActionHandler {
  operator fun invoke(action: BudgetAction)
}
