package aktual.budget.schedules.ui.search

import aktual.budget.model.ScheduleId

internal sealed interface SearchSchedulesAction

internal data object NavBack : SearchSchedulesAction

internal data object Reload : SearchSchedulesAction

@JvmInline internal value class SetQuery(val query: String) : SearchSchedulesAction

@JvmInline internal value class OpenSchedule(val id: ScheduleId) : SearchSchedulesAction
