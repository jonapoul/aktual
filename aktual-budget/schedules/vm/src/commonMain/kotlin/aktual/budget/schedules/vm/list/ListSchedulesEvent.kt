package aktual.budget.schedules.vm.list

import aktual.budget.model.TransactionId
import aktual.budget.schedules.domain.Schedule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListSchedulesEvent {
  @JvmInline value class Deleted(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class DeleteFailed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class RestoreFailed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class Completed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class CompleteFailed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class RestartFailed(val schedule: Schedule) : ListSchedulesEvent

  data class Posted(val schedule: Schedule, val transactions: ImmutableList<TransactionId>) :
    ListSchedulesEvent

  @JvmInline value class PostFailed(val schedule: Schedule) : ListSchedulesEvent

  @JvmInline value class UnpostFailed(val schedule: Schedule) : ListSchedulesEvent
}
