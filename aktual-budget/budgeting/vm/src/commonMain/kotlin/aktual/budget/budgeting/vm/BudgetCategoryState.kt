package aktual.budget.budgeting.vm

import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth

@Immutable
sealed interface BudgetCategoryState {
  data object Loading : BudgetCategoryState

  // Deleted, or the budget failed to load
  data object Failed : BudgetCategoryState

  data class Loaded(
    val type: BudgetType,
    val name: String,
    val group: String,
    val isIncome: Boolean,
    val selected: YearMonth,
    val current: YearMonth,
    // Oldest first, up to twelve months ending at the month the screen opened on
    val history: ImmutableList<CategoryHistoryMonth>,
  ) : BudgetCategoryState {
    val selectedMonth: CategoryHistoryMonth?
      get() = history.firstOrNull { it.month == selected }
  }
}

// Amounts keep their transaction sign, so spending is negative
@Immutable
data class CategoryHistoryMonth(
  val month: YearMonth,
  val budgeted: Amount,
  val spent: Amount,
  val balance: Amount,
  val carryover: Boolean,
)
