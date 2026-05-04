package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.payees.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.model.PayeeId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull

class PayeeDao(database: BudgetDatabase) {
  private val queries = database.payeesQueries

  suspend fun name(id: PayeeId): String? = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun names(ids: List<PayeeId>): List<String> = queries.withResult {
    getNames(ids).awaitAsList().map { p -> p.name ?: error("Required name for $p") }
  }

  suspend fun getAllActive(): List<GetAllActive> = queries.withResult {
    getAllActive().awaitAsList()
  }
}
