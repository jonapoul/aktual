package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Transaction_filters
import aktual.budget.db.withResult
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class TransactionFilterDao(database: BudgetDatabase) {
  private val queries = database.transactionFiltersQueries

  suspend fun getAllActive(): List<Transaction_filters> = queries.withResult {
    getAllActive().awaitAsList()
  }

  fun observeAllActive(): Flow<List<Transaction_filters>> =
    queries.getAllActive().asFlow().map { it.awaitAsList() }.distinctUntilChanged()

  suspend fun insert(filter: Transaction_filters): Long = queries.withResult {
    with(filter) { insert(id, name, conditions, conditions_op, tombstone) }
  }
}
