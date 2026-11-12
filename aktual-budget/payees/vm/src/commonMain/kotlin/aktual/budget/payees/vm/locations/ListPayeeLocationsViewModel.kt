package aktual.budget.payees.vm.locations

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.AddressLookup
import aktual.budget.payees.vm.locations.ListPayeeLocationsState.Failure
import aktual.budget.payees.vm.locations.ListPayeeLocationsState.Success
import aktual.core.model.Coordinates
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListPayeeLocationsViewModel
internal constructor(
  loader: PayeeLocationsLoader,
  private val deleter: PayeeLocationDeleter,
  private val addressLookup: AddressLookup,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val events: SharedFlow<PayeeLocationsEvent> = deleter.events

  val lookUpAddresses: StateFlow<Boolean> =
    addressLookup.isEnabled.stateIn(viewModelScope, Eagerly, initialValue = false)

  val state: StateFlow<ListPayeeLocationsState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        loader
          .observe()
          .map(::toState)
          .onStart<ListPayeeLocationsState> { emit(Loading) }
          .catch { e ->
            log.e(e) { "Failed loading payee locations" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }

  fun delete(id: PayeeLocationId) = deleter.delete(viewModelScope, id)

  fun undoDelete(id: PayeeLocationId) = deleter.restore(id)

  fun setLookUpAddresses(enabled: Boolean) {
    viewModelScope.launch { addressLookup.setEnabled(enabled) }
  }

  fun requestAddress(coordinates: Coordinates) = addressLookup.request(coordinates)

  private fun toState(locations: List<PayeeLocationItem>): ListPayeeLocationsState =
    if (locations.isEmpty()) Empty else Success(locations.toImmutableList())
}
