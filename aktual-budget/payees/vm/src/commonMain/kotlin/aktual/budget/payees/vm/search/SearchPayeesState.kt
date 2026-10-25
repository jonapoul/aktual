package aktual.budget.payees.vm.search

import aktual.budget.payees.vm.list.PayeeItem
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchPayeesState {
  data object NoQuery : SearchPayeesState

  data object Loading : SearchPayeesState

  data object NoResults : SearchPayeesState

  @JvmInline value class Failure(val cause: String?) : SearchPayeesState

  data class Results(val query: String, val payees: ImmutableList<PayeeItem>) : SearchPayeesState
}
