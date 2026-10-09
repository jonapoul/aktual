package aktual.budget.budgeting.vm

import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth

@Immutable
sealed interface BudgetState {
  data object Loading : BudgetState

  data object Failed : BudgetState

  // Amounts keep their transaction sign, so spending is negative
  data class Loaded(
    val type: BudgetType,
    val month: YearMonth,
    val summary: BudgetSummary,
    val groups: ImmutableList<GroupRow>,
    // Upstream only shows the first income group
    val income: GroupRow?,
    val banners: ImmutableList<Banner>,
    val showSpent: Boolean,
    val showHidden: Boolean,
  ) : BudgetState {
    val isEmpty: Boolean
      get() = groups.isEmpty() && income == null
  }
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
