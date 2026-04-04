package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.PayeeId

class PayeeDao(database: BudgetDatabase) {
  private val queries = database.payeesQueries

  suspend fun name(id: PayeeId): String? = queries.withResult {
    getName(id).executeAsOneOrNull()?.name
  }

  suspend fun names(ids: List<PayeeId>): List<String> = queries.withResult {
    getNames(ids).executeAsList().map { p -> p.name ?: error("Required name for $p") }
  }
}
