package aktual.budget.payees.vm.search

import aktual.budget.payees.vm.PayeesLoader
import aktual.budget.payees.vm.list.PayeeItem
import aktual.budget.payees.vm.search.SearchPayeesState.Failure
import aktual.budget.payees.vm.search.SearchPayeesState.Results
import aktual.budget.rules.domain.normalise
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import logcat.logcat

@Stable
@AssistedInject
class SearchPayeesViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  private val loader: PayeesLoader,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchPayeesViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchPayeesViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchPayeesViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val state: StateFlow<SearchPayeesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(query, searchablePayees(), ::search).catch { e ->
          logcat.e(e) { "Failed loading payees" }
          emit(Failure(e.requireMessage()))
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  fun retry() {
    retries.tryEmit(Unit)
  }

  // Null until the first load finishes
  private fun searchablePayees(): Flow<List<SearchablePayee>?> =
    loader
      .observe()
      .map<_, List<SearchablePayee>?> { payees ->
        payees.map { SearchablePayee(it, normalise(it.name)) }
      }
      .onStart { emit(null) }

  private fun search(query: String, payees: List<SearchablePayee>?): SearchPayeesState {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery
    if (payees == null) return Loading

    val normalised = normalise(trimmed)
    val matching = payees.filter { normalised in it.name }.map { it.payee }
    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private data class SearchablePayee(val payee: PayeeItem, val name: String)

  private companion object {
    const val KEY_QUERY = "query"
  }
}
