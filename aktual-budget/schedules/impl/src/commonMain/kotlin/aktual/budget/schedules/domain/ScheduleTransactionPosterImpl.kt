package aktual.budget.schedules.domain

import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
@ContributesBinding(BudgetScope::class)
class ScheduleTransactionPosterImpl(
  private val scheduleDao: ScheduleDao,
  private val transactionWriter: TransactionWriter,
  rulesLoader: TransactionRulesLoader,
  uuidGenerator: UuidGenerator,
) : ScheduleTransactionPoster {
  private val poster = SchedulePoster(rulesLoader, transactionWriter, uuidGenerator)

  override suspend fun post(id: ScheduleId, date: LocalDate): List<TransactionId> {
    val row = scheduleDao[id] ?: error("No schedule $id")
    val transaction = row.toRuleTransaction(date) ?: error("Schedule $id has no account")
    val batch = poster.batch()
    batch.add(transaction)
    return batch.post()
  }

  override suspend fun unpost(ids: List<TransactionId>) {
    if (ids.isEmpty()) return
    transactionWriter.write(runTransfers = true) { delete(ids) }
    log.i { "Unposted transactions $ids" }
  }
}
