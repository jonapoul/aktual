package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.V_schedules
import aktual.budget.db.withResult
import aktual.budget.model.ScheduleId
import aktual.budget.model.UpcomingLength
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class ScheduleDao(database: BudgetDatabase) {
  private val queries = database.schedulesQueries
  private val txQueries = database.transactionsQueries

  suspend fun name(id: ScheduleId): String? = queries.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun getAll(): List<V_schedules> = queries.withResult { getAllActive().awaitAsList() }

  suspend fun customUpcomingLength(id: ScheduleId): UpcomingLength? = queries.withResult {
    getCustomUpcomingLength(id).awaitAsOneOrNull()?.custom_upcoming_length
  }

  // fromDate is next_date for fixed schedules, next_date - 2 days for recurring
  suspend fun hasTransaction(id: ScheduleId, fromDate: LocalDate): Boolean = txQueries.withResult {
    hasTransactionForSchedule(id, fromDate).awaitAsOne()
  }
}
