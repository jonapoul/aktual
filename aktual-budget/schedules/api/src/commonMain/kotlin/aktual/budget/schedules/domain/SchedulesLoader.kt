package aktual.budget.schedules.domain

import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.UpcomingLength
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface SchedulesLoader {
  suspend fun load(): ImmutableList<Schedule>

  fun observe(): Flow<ImmutableList<Schedule>>

  fun observeUpcomingLength(): Flow<UpcomingLength>

  // The schedule that owns this rule, if any
  suspend fun ownerOf(rule: RuleId): ScheduleId?

  suspend fun load(id: ScheduleId): Schedule?
}
