package aktual.budget.schedules.vm.search

import aktual.budget.schedules.domain.Schedule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchSchedulesState {
  data object NoQuery : SearchSchedulesState

  data object Loading : SearchSchedulesState

  data object NoResults : SearchSchedulesState

  @JvmInline value class Failure(val cause: String?) : SearchSchedulesState

  data class Results(val query: String, val schedules: ImmutableList<Schedule>) :
    SearchSchedulesState
}
