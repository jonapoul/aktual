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

// Handled by the scaffold, which shows the sheet
internal data class OpenSheet(val sheet: SheetRequest) : BudgetAction

// Changes the budget's amounts
internal sealed interface BudgetWrite : BudgetAction

internal data class SetBudget(val month: YearMonth, val category: CategoryId, val input: String) :
  BudgetWrite

internal data class ApplyQuickAction(
  val month: YearMonth,
  val category: CategoryId,
  val action: QuickAction,
) : BudgetWrite

// Typed amounts are unsigned. A null category is To Budget
internal data class TransferBudget(
  val month: YearMonth,
  val input: String,
  val from: CategoryId,
  val to: CategoryId?,
) : BudgetWrite

internal data class CoverOverspending(
  val month: YearMonth,
  val to: CategoryId,
  val from: CategoryId?,
  val input: String,
) : BudgetWrite

internal data class ToggleCarryover(
  val month: YearMonth,
  val category: CategoryId,
  val enabled: Boolean,
) : BudgetWrite

internal data class HoldBudget(val month: YearMonth, val input: String) : BudgetWrite

internal data class ResetHold(val month: YearMonth) : BudgetWrite

internal data class TransferAvailable(
  val month: YearMonth,
  val input: String,
  val category: CategoryId,
) : BudgetWrite

internal data class CoverOverbudgeted(
  val month: YearMonth,
  val category: CategoryId,
  val input: String,
) : BudgetWrite

// Every category in the month, once confirmed
internal data class ApplyMonthAction(val month: YearMonth, val action: MonthAction) : BudgetWrite

// packages/desktop-client/src/components/budget/envelope/budgetsummary/BudgetMonthMenu.tsx
internal enum class MonthAction {
  CopyLastMonth,
  SetZero,
  Average3,
  Average6,
  Average12,
}

@Immutable
internal fun interface BudgetActionHandler {
  operator fun invoke(action: BudgetAction)
}
