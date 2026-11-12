package aktual.budget.payees.vm.locations

import aktual.budget.model.PayeeLocationId
import androidx.compose.runtime.Immutable

@Immutable
sealed interface PayeeLocationsEvent {
  @JvmInline value class Deleted(val id: PayeeLocationId) : PayeeLocationsEvent

  data object DeleteFailed : PayeeLocationsEvent

  data object RestoreFailed : PayeeLocationsEvent
}
