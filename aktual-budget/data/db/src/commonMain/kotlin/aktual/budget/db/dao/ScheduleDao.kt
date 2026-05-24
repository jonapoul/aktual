package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.model.ScheduleId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class ScheduleDao(database: BudgetDatabase) {
  private val schedules = database.schedulesQueries
  private val transactions = database.transactionsQueries

  suspend fun name(id: ScheduleId): String? = schedules.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun getAll(): List<GetAllActive> = schedules.withResult { getAllActive().awaitAsList() }

  // Returns the latest transaction date per schedule for transactions on or after fromDate.
  // Use the global minimum fromDate across all schedules; callers check against per-schedule
  // thresholds.
  suspend fun latestTransactionDates(fromDate: LocalDate): Map<ScheduleId, LocalDate> {
    val rows = transactions.withResult { transactionDatesFromDate(fromDate).awaitAsList() }
    val result = mutableMapOf<ScheduleId, LocalDate>()
    for (row in rows) {
      val scheduleId = row.schedule
      val date = row.date ?: continue
      val existing = result[scheduleId]
      if (existing == null || date > existing) result[scheduleId] = date
    }
    return result
  }
}
