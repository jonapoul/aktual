package aktual.budget.payees.vm.list

import aktual.budget.model.PayeeId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet

@Immutable
sealed interface ListPayeesEvent {
  @JvmInline value class Deleted(val ids: ImmutableSet<PayeeId>) : ListPayeesEvent

  data object DeleteFailed : ListPayeesEvent

  data object RestoreFailed : ListPayeesEvent

  data object UpdateFailed : ListPayeesEvent
}
