package aktual.budget.schedules.domain

import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import kotlinx.datetime.LocalDate

// Posts one schedule's transaction on demand, as postTransactionForSchedule() in
// packages/loot-core/src/server/schedules/app.ts does
interface ScheduleTransactionPoster {
  // Gives the posted transactions, which is empty if a rule deleted it
  suspend fun post(id: ScheduleId, date: LocalDate): List<TransactionId>

  // Deletes what post() added, along with the other side of any transfer
  suspend fun unpost(ids: List<TransactionId>)
}
