package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.TransactionDatesFromDate
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.ScheduleId
import aktual.budget.model.ScheduleJsonPathIndex
import aktual.budget.model.ScheduleNextDateId
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

@Inject
class ScheduleDao(database: BudgetDatabase) {
  private val schedules = database.schedulesQueries
  private val nextDates = database.schedulesNextDateQueries
  private val jsonPaths = database.schedulesJsonPathsQueries
  private val transactions = database.transactionsQueries

  suspend fun name(id: ScheduleId): String? = schedules.withResult {
    getName(id).awaitAsOneOrNull()?.name
  }

  suspend fun getAll(): List<GetAllActive> = schedules.withResult { getAllActive().awaitAsList() }

  fun observeAll(): Flow<List<GetAllActive>> =
    schedules.getAllActive().asFlow().map { it.awaitAsList() }.distinctUntilChanged()

  suspend operator fun get(id: ScheduleId): GetAllActive? = schedules.withResult {
    getActive(id, ::GetAllActive).awaitAsOneOrNull()
  }

  // The active schedule already using this name, if any
  suspend fun idByName(name: String): ScheduleId? = schedules.withResult {
    getIdByName(name).awaitAsList().firstOrNull()
  }

  suspend fun nextDateId(id: ScheduleId): ScheduleNextDateId? = nextDates.withResult {
    getBaseNextDate(id).awaitAsList().firstOrNull()?.id
  }

  // schedules_json_paths isn't synced, so it's kept up to date locally whenever a schedule's rule
  // changes. Mirrors onRuleUpdate() in packages/loot-core/src/server/schedules/app.ts
  suspend fun setJsonPaths(
    id: ScheduleId,
    payee: ScheduleJsonPathIndex?,
    account: ScheduleJsonPathIndex?,
    amount: ScheduleJsonPathIndex?,
    date: ScheduleJsonPathIndex?,
  ) = jsonPaths.withoutResult {
    insert(schedule_id = id, payee = payee, account = account, amount = amount, date = date)
  }

  // Returns the latest transaction date per schedule for transactions on or after fromDate.
  // Use the global minimum fromDate across all schedules; callers check against per-schedule
  // thresholds.
  // Returns the latest transaction date per schedule for transactions on or after fromDate.
  // Use the global minimum fromDate across all schedules; callers check against per-schedule
  // thresholds.
  suspend fun latestTransactionDates(fromDate: LocalDate): Map<ScheduleId, LocalDate> =
    transactions.withResult { transactionDatesFromDate(fromDate).awaitAsList() }.latestDates()

  fun observeLatestTransactionDates(fromDate: LocalDate): Flow<Map<ScheduleId, LocalDate>> =
    transactions
      .transactionDatesFromDate(fromDate)
      .asFlow()
      .map { it.awaitAsList().latestDates() }
      .distinctUntilChanged()

  private fun List<TransactionDatesFromDate>.latestDates(): Map<ScheduleId, LocalDate> {
    val result = mutableMapOf<ScheduleId, LocalDate>()
    for (row in this) {
      val scheduleId = row.schedule
      val date = row.date ?: continue
      val existing = result[scheduleId]
      if (existing == null || date > existing) result[scheduleId] = date
    }
    return result
  }
}
