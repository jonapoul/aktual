package aktual.budget.budgeting.vm

import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus

@Immutable
sealed interface BudgetState {
  data object Loading : BudgetState

  data object Failed : BudgetState

  data class Loaded(
    val type: BudgetType,
    // The first visible month
    val month: YearMonth,
    val current: YearMonth,
    val earliest: YearMonth,
    val latest: YearMonth,
    // How many months are visible, and the most that fit
    val monthCount: Int,
    val maxMonthCount: Int,
    // The visible months and their neighbours. One that hasn't loaded yet is missing
    val months: ImmutableList<MonthBudget>,
    val showSpent: Boolean,
    val showHidden: Boolean,
  ) : BudgetState {
    val lastMonth: YearMonth
      get() = month.plus(monthCount - 1, MONTH)

    val canGoBack: Boolean
      get() = month > earliest

    val canGoForward: Boolean
      get() = lastMonth < latest

    val isEmpty: Boolean
      get() = months.firstOrNull()?.isEmpty == true

    operator fun get(month: YearMonth): MonthBudget? = months.firstOrNull { it.month == month }
  }
}

// Amounts keep their transaction sign, so spending is negative
@Immutable
data class MonthBudget(
  val month: YearMonth,
  val summary: BudgetSummary,
  val groups: ImmutableList<GroupRow>,
  // Upstream only shows the first income group
  val income: GroupRow?,
  val banners: ImmutableList<Banner>,
) {
  val isEmpty: Boolean
    get() = groups.isEmpty() && income == null
}

@Immutable
sealed interface BudgetSummary {
  data class Envelope(val toBudget: Amount, val available: Amount, val budgeted: Amount) :
    BudgetSummary

  // packages/desktop-client/src/components/mobile/budget/BudgetTable.tsx Saved. Months that
  // haven't finished show what the budget is set to save, others what was actually saved
  data class Tracking(
    val saved: Amount,
    val isProjected: Boolean,
    val budgeted: Amount,
    val spent: Amount,
  ) : BudgetSummary
}

@Immutable
data class GroupRow(
  val id: CategoryGroupId,
  val name: String,
  val isHidden: Boolean,
  val isCollapsed: Boolean,
  val budgeted: Amount,
  val spent: Amount,
  val balance: Amount,
  val categories: ImmutableList<CategoryRow>,
)

@Immutable
data class CategoryRow(
  val id: CategoryId,
  val name: String,
  // Also set when its group is hidden
  val isHidden: Boolean,
  val budgeted: Amount,
  val spent: Amount,
  val balance: Amount,
  val carryover: Boolean,
)

// packages/desktop-client/src/components/mobile/budget/BudgetPage.tsx Banners
@Immutable
sealed interface Banner {
  data class Uncategorised(val count: Int) : Banner

  data class Overspent(val count: Int, val total: Amount) : Banner

  data class Overbudgeted(val amount: Amount) : Banner
}
