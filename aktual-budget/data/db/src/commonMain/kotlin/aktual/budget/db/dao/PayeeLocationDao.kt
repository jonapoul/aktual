package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetForPayee
import aktual.budget.db.GetInBounds
import aktual.budget.db.withResult
import aktual.budget.model.PayeeId
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject

@Inject
class PayeeLocationDao(database: BudgetDatabase) {
  private val queries = database.payeeLocationsQueries

  suspend fun forPayee(payee: PayeeId): List<GetForPayee> = queries.withResult {
    getForPayee(payee).awaitAsList()
  }

  suspend fun inBounds(
    minLatitude: Double,
    maxLatitude: Double,
    minLongitude: Double,
    maxLongitude: Double,
  ): List<GetInBounds> = queries.withResult {
    getInBounds(minLatitude, maxLatitude, minLongitude, maxLongitude).awaitAsList()
  }
}
