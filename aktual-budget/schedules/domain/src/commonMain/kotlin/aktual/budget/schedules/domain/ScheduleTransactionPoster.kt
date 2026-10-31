package aktual.budget.schedules.domain

import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate
import logcat.logcat

// Posts one schedule's transaction on demand, as postTransactionForSchedule() in
// packages/loot-core/src/server/schedules/app.ts does
@Inject
class ScheduleTransactionPoster(
  private val scheduleDao: ScheduleDao,
  private val transactionWriter: TransactionWriter,
  rulesLoader: TransactionRulesLoader,
  uuidGenerator: UuidGenerator,
) {
  private val poster = SchedulePoster(rulesLoader, transactionWriter, uuidGenerator)

  // Gives the posted transactions, which is empty if a rule deleted it
  suspend fun post(id: ScheduleId, date: LocalDate): List<TransactionId> {
    val row = scheduleDao[id] ?: error("No schedule $id")
    val transaction = row.toRuleTransaction(date) ?: error("Schedule $id has no account")
    val batch = poster.batch()
    batch.add(transaction)
    return batch.post()
  }

  // Deletes what post() added, along with the other side of any transfer
  suspend fun unpost(ids: List<TransactionId>) {
    if (ids.isEmpty()) return
    transactionWriter.write(runTransfers = true) { delete(ids) }
    logcat.i { "Unposted transactions $ids" }
  }
}
