package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.FormulaAccounts
import aktual.budget.db.FormulaCategories
import aktual.budget.db.FormulaGoals
import aktual.budget.db.FormulaTransactions
import aktual.budget.db.withResult
import aktual.budget.model.AccountId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.coroutines.asFlow
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

@Inject
class FormulaDao(database: BudgetDatabase) {
  private val queries = database.formulasQueries

  // Emits now and whenever anything a formula reads changes
  fun observeChanges(): Flow<Unit> = queries.formulaChanges().asFlow().map {}

  suspend fun transactions(start: LocalDate, end: LocalDate): List<FormulaTransactions> =
    queries.withResult {
      formulaTransactions(start, end).awaitAsList()
    }

  suspend fun categories(): List<FormulaCategories> = queries.withResult {
    formulaCategories().awaitAsList()
  }

  suspend fun accounts(): List<FormulaAccounts> = queries.withResult {
    formulaAccounts().awaitAsList()
  }

  suspend fun accountBalance(account: AccountId): Long = queries.withResult {
    formulaAccountBalance(account).awaitAsOne()
  }

  suspend fun goals(month: YearMonth): List<FormulaGoals> = queries.withResult {
    formulaGoals(month).awaitAsList()
  }
}
