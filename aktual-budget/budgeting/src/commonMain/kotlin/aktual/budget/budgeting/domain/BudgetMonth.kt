package aktual.budget.budgeting.domain

import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth

// Amounts keep their transaction sign, so spending is negative
sealed interface BudgetMonth {
  val month: YearMonth
  val budgeted: Amount
  val spent: Amount
  val balance: Amount
  val income: Amount
  val categories: ImmutableList<CategoryMonth>
  val groups: ImmutableList<CategoryGroupMonth>

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
    override val groups: ImmutableList<CategoryGroupMonth> = persistentListOf(),
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
    override val groups: ImmutableList<CategoryGroupMonth> = persistentListOf(),
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

// Totals follow upstream's group-budget-*, group-sum-amount-* and group-leftover-* cells
data class CategoryGroupMonth(
  val id: CategoryGroupId,
  val name: String,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val sortOrder: Double?,
  val budgeted: Amount,
  val spent: Amount,
  val balance: Amount,
  val categories: ImmutableList<CategoryMonth>,
)
