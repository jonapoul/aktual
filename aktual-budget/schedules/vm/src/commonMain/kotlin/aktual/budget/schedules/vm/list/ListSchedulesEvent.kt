package aktual.budget.schedules.vm.list

import aktual.budget.schedules.vm.Schedule
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListSchedulesEvent {
  data class Deleted(val schedule: Schedule, val index: Int) : ListSchedulesEvent

  @JvmInline value class DeleteFailed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class RestoreFailed(val schedule: Schedule) : ListSchedulesEvent
}
