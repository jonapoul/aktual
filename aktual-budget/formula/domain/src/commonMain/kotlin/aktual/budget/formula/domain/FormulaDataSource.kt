package aktual.budget.formula.domain

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
}

data class UnknownQuery(val name: String) : FormulaWarning

data class UnknownAccount(val account: String) : FormulaWarning
