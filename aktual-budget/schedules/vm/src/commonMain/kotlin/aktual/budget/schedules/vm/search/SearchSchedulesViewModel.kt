package aktual.budget.schedules.vm.search

import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.budget.schedules.vm.search.SearchSchedulesState.Failure
import aktual.budget.schedules.vm.search.SearchSchedulesState.Results
import aktual.di.BudgetScope
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class SearchSchedulesViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  private val loader: SchedulesLoader,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchSchedulesViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchSchedulesViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchSchedulesViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val schedules = MutableStateFlow<List<Schedule>?>(null)
  private val failure = MutableStateFlow<String?>(null)

  val state: StateFlow<SearchSchedulesState> =
    combine(query, schedules, failure, ::search)
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  init {
    reload()
  }

  fun reload() {
    failure.update { null }
    viewModelScope.launch {
      try {
        val loaded = loader.load()
        schedules.update { loaded }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading schedules" }
        failure.update { e.requireMessage() }
      }
    }
  }

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  private fun search(
    query: String,
    schedules: List<Schedule>?,
    failure: String?,
  ): SearchSchedulesState {
    if (failure != null) return Failure(failure)
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
