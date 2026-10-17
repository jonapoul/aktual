package aktual.budget.formula

import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

// Budget data for the query functions. Amounts are in minor units, and each function returns null
// when there's no query or account by that name
interface FormulaDataSource {
  suspend fun querySum(name: String): Long?

  suspend fun queryCount(name: String): Long?

  suspend fun queryCategories(name: String): List<String>?

  // A query without a time frame covers the current month
  suspend fun queryMonths(name: String): YearMonthRange?

  // [account] is an account id or name
  suspend fun accountBalance(account: String): Long?

  // Envelope budget values by category id, categories without any are left out
  suspend fun budgetMonth(month: YearMonth): Map<String, CategoryBudget>
}

data class CategoryBudget(
  val budgeted: Long,
  val spent: Long,
  val goal: Long,
  val balance: Long,
  val carryover: Boolean,
)

data class UnknownQuery(val name: String) : FormulaWarning

data class UnknownAccount(val account: String) : FormulaWarning
