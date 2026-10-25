package aktual.budget.payees.ui.search

import aktual.budget.payees.vm.list.PayeeItem
import androidx.compose.runtime.Immutable

internal sealed interface SearchPayeesAction

internal data object NavBack : SearchPayeesAction

internal data object Retry : SearchPayeesAction

@JvmInline internal value class SetQuery(val query: String) : SearchPayeesAction

@JvmInline internal value class OpenRules(val payee: PayeeItem) : SearchPayeesAction

@Immutable
internal fun interface SearchPayeesActionHandler {
  operator fun invoke(action: SearchPayeesAction)
}
