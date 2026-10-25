package aktual.budget.payees.vm.list

import aktual.budget.payees.domain.PayeeMerge
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListPayeesEvent {
  @JvmInline value class Deleted(val payee: PayeeItem) : ListPayeesEvent

  data object DeleteFailed : ListPayeesEvent

  data class Merged(val payee: PayeeItem, val target: PayeeItem, val merge: PayeeMerge) :
    ListPayeesEvent

  data object MergeFailed : ListPayeesEvent

  data object RestoreFailed : ListPayeesEvent

  data object UpdateFailed : ListPayeesEvent
}
