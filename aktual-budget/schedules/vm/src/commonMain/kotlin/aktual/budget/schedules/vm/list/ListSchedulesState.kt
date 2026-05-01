package aktual.budget.schedules.vm.list

import aktual.budget.schedules.vm.Schedule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface ListSchedulesState

data object Loading : ListSchedulesState

@JvmInline value class Failure(val cause: String?) : ListSchedulesState

data object Empty : ListSchedulesState

@JvmInline value class Success(val schedules: ImmutableList<Schedule>) : ListSchedulesState
