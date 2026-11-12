package aktual.budget.payees.vm.locations

import aktual.budget.model.PayeeLocationId
import aktual.budget.payees.domain.PayeeLocations
import aktual.budget.payees.vm.locations.PayeeLocationsEvent.Deleted
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.log
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Shared by the list and search screens, which both delete with an undo snackbar
@Inject
internal class PayeeLocationDeleter(
  private val locations: PayeeLocations,
  private val budgetScope: BudgetCoroutineScope,
) {
  private val mutableEvents =
    MutableSharedFlow<PayeeLocationsEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<PayeeLocationsEvent> = mutableEvents.asSharedFlow()

  fun delete(scope: CoroutineScope, id: PayeeLocationId) {
    scope.launch {
      runCatchingCancellable(
        attempt = {
          locations.delete(id)
          mutableEvents.tryEmit(Deleted(id))
        },
        onFailure = { e ->
          log.e(e) { "Failed deleting location $id" }
          mutableEvents.tryEmit(DeleteFailed)
        },
      )
    }
  }

  // The undo snackbar can outlive the screen, so the restore can't be tied to its scope
  fun restore(id: PayeeLocationId) {
    budgetScope.launch {
      runCatchingCancellable(
        attempt = { locations.restore(id) },
        onFailure = { e ->
          log.e(e) { "Failed restoring location $id" }
          mutableEvents.tryEmit(RestoreFailed)
        },
      )
    }
  }
}
