package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.withResult
import aktual.budget.model.ScheduleId

class ScheduleDao(database: BudgetDatabase) {
  private val queries = database.schedulesQueries

  suspend fun name(id: ScheduleId): String? = queries.withResult {
    getName(id).executeAsOneOrNull()?.name
  }
}
