package aktual.budget.schedules.ui.list

import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ListSchedulesAction

internal data object Reload : ListSchedulesAction

internal data object CreateNew : ListSchedulesAction

@JvmInline internal value class Open(val id: ScheduleId) : ListSchedulesAction

@Immutable
internal fun interface ListSchedulesActionHandler {
  operator fun invoke(action: ListSchedulesAction)
}
