package aktual.budget.home.vm

import aktual.budget.schedules.domain.SchedulesLoader
import aktual.budget.schedules.domain.upcoming
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Inject
internal class UpcomingSchedulesLoader(
  private val schedulesLoader: SchedulesLoader,
  private val calendar: Calendar,
) {
  fun observe(): Flow<UpcomingSchedules> =
    combine(schedulesLoader.observe(), schedulesLoader.observeUpcomingLength()) { schedules, length
      ->
      val today = calendar.today()
      UpcomingSchedules(length, today, schedules.upcoming(today, length))
    }
}
