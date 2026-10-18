package aktual.about.vm.logs

import aktual.core.logging.LogLine
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.datetime.LocalDate
import logcat.LogPriority

@Immutable
data class LogFilters(
  val query: String,
  val priorities: ImmutableSet<LogPriority>,
  val tags: ImmutableSet<String>,
) {
  companion object {
    val Default =
      LogFilters(
        query = "",
        priorities = LogPriority.entries.filter { it != VERBOSE }.toImmutableSet(),
        tags = persistentSetOf(),
      )
  }
}

@Immutable
sealed interface LogEntriesState {
  data object Loading : LogEntriesState

  data class Loaded(
    val date: LocalDate,
    val today: LocalDate,
    val lines: ImmutableList<LogLine>,
    val counts: ImmutableMap<LogPriority, Int>,
    val live: LiveState,
  ) : LogEntriesState
}

@Immutable
sealed interface LiveState {
  // An older file, which won't change
  data object None : LiveState

  data object Following : LiveState

  data class Paused(val pending: Int) : LiveState
}
