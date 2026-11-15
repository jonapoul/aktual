package aktual.budget.schedules.domain

import aktual.budget.model.UpcomingLength
import aktual.budget.model.upcomingDays
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

// Missed, due and upcoming schedules up to the end of the window, soonest first
fun List<Schedule>.upcoming(today: LocalDate, length: UpcomingLength): ImmutableList<Schedule> {
  val end = today.plus(value = length.upcomingDays(today), unit = DAY)
  return filter { it.status in UpcomingStatuses && it.nextDate <= end }
    .sortedBy { it.nextDate }
    .toImmutableList()
}

private val UpcomingStatuses = setOf<ScheduleStatus>(Missed, Due, Upcoming)
