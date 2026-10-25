package aktual.budget.payees.vm.list

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListPayeesEvent {
  @JvmInline value class Deleted(val payee: PayeeItem) : ListPayeesEvent

  data object DeleteFailed : ListPayeesEvent

  data object RestoreFailed : ListPayeesEvent

  data object UpdateFailed : ListPayeesEvent
}
