package aktual.budget.schedules.vm.list

import aktual.budget.schedules.domain.Schedule
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListSchedulesEvent {
  @JvmInline value class Deleted(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class DeleteFailed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class RestoreFailed(val schedule: Schedule) : ListSchedulesEvent
}
