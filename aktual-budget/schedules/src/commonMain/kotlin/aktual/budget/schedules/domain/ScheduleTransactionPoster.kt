package aktual.budget.schedules.domain

import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import kotlinx.datetime.LocalDate

// Posts one schedule's transaction on demand, as postTransactionForSchedule() in
// packages/loot-core/src/server/schedules/app.ts does
interface ScheduleTransactionPoster {
  // Posting before the schedule is due pays that occurrence, so the schedule moves on to the next
  // one, or completes if there isn't one
  suspend fun post(id: ScheduleId, date: LocalDate): PostedSchedule

  // Deletes what post() added, along with the other side of any transfer, and puts the schedule
  // back how it was
  suspend fun unpost(posted: PostedSchedule)
}

data class PostedSchedule(
  val id: ScheduleId,
  // Without split children, and empty if a rule deleted the transaction
  val transactions: List<TransactionId>,
  // The schedule's next date before posting moved it on
  val movedFrom: LocalDate? = null,
  val completed: Boolean = false,
) {
  val canUndo: Boolean
    get() = transactions.isNotEmpty() || movedFrom != null || completed
}
