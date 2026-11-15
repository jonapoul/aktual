package aktual.budget.schedules.vm.search

import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.budget.schedules.vm.search.SearchSchedulesState.Failure
import aktual.budget.schedules.vm.search.SearchSchedulesState.Results
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@Stable
@AssistedInject
class SearchSchedulesViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  private val loader: SchedulesLoader,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchSchedulesViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchSchedulesViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchSchedulesViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val state: StateFlow<SearchSchedulesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(query, schedules(), ::search).catch { e ->
          log.e(e) { "Failed loading schedules" }
          emit(Failure(e.requireMessage()))
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  fun reload() {
    retries.tryEmit(Unit)
  }

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  // Null until the first load finishes
  private fun schedules(): Flow<List<Schedule>?> =
    loader.observe().onStart<List<Schedule>?> { emit(null) }

  private fun search(query: String, schedules: List<Schedule>?): SearchSchedulesState {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery
    if (schedules == null) return Loading

    val matching = schedules.filter { it.matches(trimmed) }
    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private fun Schedule.matches(query: String): Boolean =
    name?.contains(query, ignoreCase = true) == true ||
      payeeName.contains(query, ignoreCase = true) ||
      accountName.contains(query, ignoreCase = true)

  private companion object {
    const val KEY_QUERY = "query"
  }
}
