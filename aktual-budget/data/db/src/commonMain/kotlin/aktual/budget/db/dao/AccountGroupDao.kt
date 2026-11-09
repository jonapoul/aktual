package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.accountGroups.GetAllActive
import aktual.budget.db.withResult
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class AccountGroupDao(database: BudgetDatabase) {
  private val queries = database.accountGroupsQueries

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }

  fun observeAllActive(): Flow<List<GetAllActive>> =
    queries.getAllActive().asFlow().map { it.awaitAsList() }.distinctUntilChanged()

  // The highest sort order among live groups, null if there are none
  suspend fun maxSortOrder(): Double? = queries.withResult {
    getMaxSortOrder().awaitAsOneOrNull()?.sort_order
  }
}
