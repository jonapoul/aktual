package aktual.budget.schedules.domain

import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.model.LocalChange
import aktual.budget.model.ScheduleId
import aktual.budget.model.nextDateAfter
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.utils.Calendar
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
  private val calendar: Calendar,
  rulesLoader: TransactionRulesLoader,
  uuidGenerator: UuidGenerator,
) : ScheduleTransactionPoster {
  private val poster = SchedulePoster(rulesLoader, transactionWriter, uuidGenerator)

  // The schedule moves on in the same sync as the transaction, so neither goes out without the
  // other
  override suspend fun post(id: ScheduleId, date: LocalDate): PostedSchedule {
    val row = scheduleDao[id] ?: error("No schedule $id")
    val transaction = row.toRuleTransaction(date) ?: error("Schedule $id has no account")
    // Read the day once, since it can change while the transaction is saved
    val result = row.moveOn(today = calendar.today())
    val batch = poster.batch()
    batch.add(transaction)
    val transactions = batch.post(result.changes)
    log.i { "Posted schedule $id: $result" }
    return PostedSchedule(id, transactions, result.movedFrom, result.completed)
  }

  override suspend fun unpost(posted: PostedSchedule) {
    val changes = buildList {
      posted.movedFrom?.let { addAll(scheduleDao.localNextDateChanges(posted.id, it)) }
      if (posted.completed) add(completedChange(posted.id, completed = false))
    }
    transactionWriter.write(runTransfers = true) {
      if (posted.transactions.isNotEmpty()) delete(posted.transactions)
      add(changes)
    }
    log.i { "Unposted schedule ${posted.id}: $posted" }
  }

  // Posting before the next date pays that occurrence. Without moving on, it shows as due again
  // whenever the transaction is dated before it
  private suspend fun GetAllActive.moveOn(today: LocalDate): MoveOn {
    val nextDate = next_date
    val scheduleDate = parseDate()
    if (nextDate == null || scheduleDate == null || nextDate <= today) return MoveOn()

    val after = (scheduleDate as? Recurring)?.config?.nextDateAfter(nextDate)
    return if (after != null) {
      MoveOn(scheduleDao.localNextDateChanges(id, after), movedFrom = nextDate)
    } else {
      MoveOn(listOf(completedChange(id, completed = true)), completed = true)
    }
  }

  private data class MoveOn(
    val changes: List<LocalChange> = emptyList(),
    val movedFrom: LocalDate? = null,
    val completed: Boolean = false,
  )
}
