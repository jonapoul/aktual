package aktual.budget.home.domain

import aktual.budget.model.Amount
import aktual.budget.model.UpcomingLength
import aktual.budget.schedules.domain.Schedule
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class UpcomingSchedules(
  val length: UpcomingLength,
  val today: LocalDate,
  val schedules: ImmutableList<Schedule>,
) {
  val total: Amount
    get() = schedules.fold(Amount.Zero) { sum, schedule -> sum + schedule.amount }
}
