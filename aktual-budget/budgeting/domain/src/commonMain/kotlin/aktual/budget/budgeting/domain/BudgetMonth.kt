package aktual.budget.budgeting.domain

import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.YearMonth

// Amounts keep their transaction sign, so spending is negative
sealed interface BudgetMonth {
  val month: YearMonth
  val budgeted: Amount
  val spent: Amount
  val balance: Amount
  val income: Amount
  val categories: ImmutableList<CategoryMonth>

  data class Envelope(
    override val month: YearMonth,
    val toBudget: Amount,
    override val budgeted: Amount,
    override val spent: Amount,
    override val balance: Amount,
    override val income: Amount,
    val fromLastMonth: Amount,
    val lastMonthOverspent: Amount,
    // Income held back for next month
    val buffered: Amount,
    override val categories: ImmutableList<CategoryMonth>,
  ) : BudgetMonth {
    val availableFunds: Amount
      get() = income + fromLastMonth
  }

  data class Tracking(
    override val month: YearMonth,
    override val budgeted: Amount,
    override val spent: Amount,
    override val balance: Amount,
    override val income: Amount,
    val incomeBudgeted: Amount,
    override val categories: ImmutableList<CategoryMonth>,
  ) : BudgetMonth
}

data class CategoryMonth(
  val id: CategoryId,
  val name: String,
  val group: CategoryGroupId,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val budgeted: Amount,
  val spent: Amount,
  val balance: Amount,
  val carryover: Boolean,
)
