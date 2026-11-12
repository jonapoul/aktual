package aktual.budget.payees.vm.locations

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchPayeeLocationsState {
  data object NoQuery : SearchPayeeLocationsState

  data object Loading : SearchPayeeLocationsState

  data object NoResults : SearchPayeeLocationsState

  @JvmInline value class Failure(val cause: String?) : SearchPayeeLocationsState

  data class Results(val query: String, val locations: ImmutableList<PayeeLocationItem>) :
    SearchPayeeLocationsState
}
