package aktual.budget.schedules.vm.list

import aktual.budget.schedules.vm.Schedule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface ListSchedulesState

data object Loading : ListSchedulesState

@JvmInline value class Failure(val cause: String?) : ListSchedulesState

// No schedules exist at all
data object Empty : ListSchedulesState

// Can be empty if there are schedules, but no matches with the given filter
@Immutable
data class Success(
  val schedules: ImmutableList<Schedule>,
  val filterText: String,
  val isSearchActive: Boolean,
) : ListSchedulesState
