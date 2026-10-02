package aktual.budget.schedules.ui.list

import aktual.budget.model.ScheduleId
import aktual.budget.schedules.vm.Schedule
import androidx.compose.runtime.Immutable

internal sealed interface ListSchedulesAction

internal data object Reload : ListSchedulesAction

internal data object CreateNew : ListSchedulesAction

@JvmInline internal value class Open(val id: ScheduleId) : ListSchedulesAction

internal data object OpenSearch : ListSchedulesAction

@JvmInline internal value class Delete(val schedule: Schedule) : ListSchedulesAction

@JvmInline internal value class Post(val schedule: Schedule) : ListSchedulesAction

internal data object OpenSettings : ListSchedulesAction

@Immutable
internal fun interface ListSchedulesActionHandler {
  operator fun invoke(action: ListSchedulesAction)
}
