package aktual.budget.payees.vm.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface ListPayeesState

data object Loading : ListPayeesState

@JvmInline value class Failure(val cause: String?) : ListPayeesState

data object Empty : ListPayeesState

@JvmInline value class Success(val payees: ImmutableList<PayeeItem>) : ListPayeesState
