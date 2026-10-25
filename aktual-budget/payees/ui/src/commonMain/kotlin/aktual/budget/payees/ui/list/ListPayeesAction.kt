package aktual.budget.payees.ui.list

import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.list.PayeeItem
import androidx.compose.runtime.Immutable

internal sealed interface ListPayeesAction

internal data object Retry : ListPayeesAction

internal data object OpenSearch : ListPayeesAction

@JvmInline internal value class OpenPayee(val id: PayeeId) : ListPayeesAction

@JvmInline internal value class OpenRules(val payee: PayeeItem) : ListPayeesAction

@JvmInline internal value class Delete(val payee: PayeeItem) : ListPayeesAction

@JvmInline internal value class ToggleFavorite(val payee: PayeeItem) : ListPayeesAction

@Immutable
internal fun interface ListPayeesActionHandler {
  operator fun invoke(action: ListPayeesAction)
}
