package aktual.budget.payees.vm.locations

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.AddressLookup
import aktual.budget.payees.domain.AddressResult.Found
import aktual.budget.payees.vm.locations.SearchPayeeLocationsState.Failure
import aktual.budget.payees.vm.locations.SearchPayeeLocationsState.Results
import aktual.budget.rules.domain.normalise
import aktual.core.model.Coordinates
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@Stable
@AssistedInject
class SearchPayeeLocationsViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  private val loader: PayeeLocationsLoader,
  private val deleter: PayeeLocationDeleter,
  private val addressLookup: AddressLookup,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchPayeeLocationsViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchPayeeLocationsViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchPayeeLocationsViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  val events: SharedFlow<PayeeLocationsEvent> = deleter.events

  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val state: StateFlow<SearchPayeeLocationsState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(query, searchableLocations(), ::search).catch { e ->
          log.e(e) { "Failed loading payee locations" }
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

  fun delete(id: PayeeLocationId) = deleter.delete(viewModelScope, id)

  fun undoDelete(id: PayeeLocationId) = deleter.restore(id)

  fun requestAddress(coordinates: Coordinates) = addressLookup.request(coordinates)

  // Null until the first load finishes
  private fun searchableLocations(): Flow<List<SearchableLocation>?> =
    loader
      .observe()
      .map<_, List<SearchableLocation>?> { locations ->
        locations.map { location ->
          val address = (location.address as? Found)?.address?.title
          SearchableLocation(location, normalise(location.payeeName), address?.let(::normalise))
        }
      }
      .onStart { emit(null) }

  private fun search(
    query: String,
    locations: List<SearchableLocation>?,
  ): SearchPayeeLocationsState {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery
    if (locations == null) return Loading

    val normalised = normalise(trimmed)
    val matching =
      locations
        .filter { normalised in it.payeeName || it.address?.contains(normalised) == true }
        .map { it.location }
    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private data class SearchableLocation(
    val location: PayeeLocationItem,
    val payeeName: String,
    val address: String?,
  )

  private companion object {
    const val KEY_QUERY = "query"
  }
}
