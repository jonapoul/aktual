package aktual.about.vm.logs

import aktual.about.data.LogsRepository
import aktual.core.logging.LogLine
import aktual.di.AppScope
import alakazam.kotlin.CoroutineContexts
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import logcat.LogPriority

@Stable
@AssistedInject
class LogEntriesViewModel
internal constructor(
  @Assisted private val date: LocalDate,
  @Assisted initialFilters: LogFilters,
  private val repository: LogsRepository,
  private val exporter: LogExporter,
  contexts: CoroutineContexts,
) : ViewModel() {
  // Number of lines shown when paused, or null when following
  private val pausedAt = MutableStateFlow<Int?>(null)

  private val mutableFilters = MutableStateFlow(initialFilters)
  val filters: StateFlow<LogFilters> = mutableFilters.asStateFlow()

  private val allLines: StateFlow<List<LogLine>?> =
    repository.watch(date).stateIn(viewModelScope, Eagerly, initialValue = null)

  val state: StateFlow<LogEntriesState> =
    combine(allLines, pausedAt, mutableFilters, ::buildState)
      .flowOn(contexts.default)
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  private val exportChannel = Channel<LogExport>(BUFFERED)
  val exports: Flow<LogExport> = exportChannel.receiveAsFlow()

  fun setQuery(query: String) = mutableFilters.update { it.copy(query = query) }

  fun togglePriority(priority: LogPriority) = mutableFilters.update { filters ->
    val priorities = filters.priorities
    val updated = if (priority in priorities) priorities - priority else priorities + priority
    filters.copy(priorities = updated.toImmutableSet())
  }

  fun addTag(tag: String) = mutableFilters.update {
    it.copy(tags = (it.tags + tag).toImmutableSet())
  }

  fun removeTag(tag: String) = mutableFilters.update {
    it.copy(tags = (it.tags - tag).toImmutableSet())
  }

  fun pause() {
    val count = allLines.value?.size ?: return
    pausedAt.update { count }
  }

  fun resume() = pausedAt.update { null }

  fun export(scope: LogExportScope, target: LogExportTarget) {
    viewModelScope.launch {
      val loaded = state.value as? Loaded
      val export =
        exporter.export(
          scope = scope,
          target = target,
          shown = loaded?.lines.orEmpty(),
          shownDate = date,
        )
      exportChannel.send(export)
    }
  }

  private fun buildState(
    lines: List<LogLine>?,
    pausedCount: Int?,
    filters: LogFilters,
  ): LogEntriesState {
    if (lines == null) return Loading
    // The file can shrink while paused, e.g. when deleted
    val paused = pausedCount?.coerceAtMost(lines.size)
    val visible = if (paused == null) lines else lines.take(paused)

    val query = filters.query.trim()
    val matching = visible.filter { line ->
      line.priority in filters.priorities &&
        (filters.tags.isEmpty() || line.tag in filters.tags) &&
        (query.isEmpty() || line.matches(query))
    }

    val today = repository.today()
    val live: LiveState =
      when {
        date != today -> None
        paused == null -> Following
        else -> LiveState.Paused(pending = lines.size - paused)
      }

    return LogEntriesState.Loaded(
      date = date,
      today = today,
      lines = matching.toImmutableList(),
      counts = visible.groupingBy { it.priority }.eachCount().toImmutableMap(),
      live = live,
    )
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted date: LocalDate,
      @Assisted initialFilters: LogFilters,
    ): LogEntriesViewModel
  }

  private fun LogLine.matches(query: String): Boolean =
    message.contains(query, ignoreCase = true) || tag?.contains(query, ignoreCase = true) == true
}
