package aktual.budget.payees.vm.locations

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListPayeeLocationsState {
  data object Loading : ListPayeeLocationsState

  data object Empty : ListPayeeLocationsState

  @JvmInline value class Failure(val cause: String?) : ListPayeeLocationsState

  @JvmInline
  value class Success(val locations: ImmutableList<PayeeLocationItem>) : ListPayeeLocationsState
}
