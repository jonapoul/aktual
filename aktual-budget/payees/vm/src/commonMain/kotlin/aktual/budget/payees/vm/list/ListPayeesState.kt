package aktual.budget.payees.vm.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable sealed interface ListPayeesState

data object Loading : ListPayeesState

@JvmInline value class Failure(val cause: String?) : ListPayeesState

data object Empty : ListPayeesState

// payees holds every payee, so merging can pick any of them, and shown is what the list displays
// isLearningEnabled is the global learn-categories pref
data class Success(
  val payees: ImmutableList<PayeeItem>,
  val unusedOnly: Boolean = false,
  val isLearningEnabled: Boolean = true,
) : ListPayeesState {
  val numUnused: Int = payees.count { it.isUnused }
  val shown: ImmutableList<PayeeItem> =
    if (unusedOnly) payees.filter { it.isUnused }.toImmutableList() else payees
}
