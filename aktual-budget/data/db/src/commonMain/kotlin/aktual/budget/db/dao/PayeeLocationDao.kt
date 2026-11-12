package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetAllWithPayee
import aktual.budget.db.GetForPayee
import aktual.budget.db.GetInBounds
import aktual.budget.db.withResult
import aktual.budget.model.PayeeId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Inject
class PayeeLocationDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.payeeLocationsQueries

  suspend fun forPayee(payee: PayeeId): List<GetForPayee> = queries.withResult {
    getForPayee(payee).awaitAsList()
  }

  fun observeForPayee(payee: PayeeId): Flow<List<GetForPayee>> =
    queries.getForPayee(payee).asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeAll(): Flow<List<GetAllWithPayee>> =
    queries.getAllWithPayee().asFlow().mapToList(contexts.default).distinctUntilChanged()

  suspend fun inBounds(
    minLatitude: Double,
    maxLatitude: Double,
    minLongitude: Double,
    maxLongitude: Double,
  ): List<GetInBounds> = queries.withResult {
    getInBounds(minLatitude, maxLatitude, minLongitude, maxLongitude).awaitAsList()
  }
}
