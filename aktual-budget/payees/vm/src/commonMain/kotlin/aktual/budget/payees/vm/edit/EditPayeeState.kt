package aktual.budget.payees.vm.edit

import aktual.budget.payees.vm.list.PayeeItem
import androidx.compose.runtime.Immutable

@Immutable sealed interface EditPayeeState

data object Loading : EditPayeeState

// The payee as it's stored, kept up to date so the rule and transaction counts stay right
@JvmInline value class Editing(val payee: PayeeItem) : EditPayeeState

@JvmInline value class Failure(val cause: String?) : EditPayeeState
