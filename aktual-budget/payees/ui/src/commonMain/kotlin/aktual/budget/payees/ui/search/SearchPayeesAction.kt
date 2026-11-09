package aktual.budget.payees.ui.search

import aktual.budget.model.PayeeId
import aktual.budget.payees.vm.list.PayeeItem

internal sealed interface SearchPayeesAction

internal data object NavBack : SearchPayeesAction

internal data object Retry : SearchPayeesAction

@JvmInline internal value class SetQuery(val query: String) : SearchPayeesAction

@JvmInline internal value class OpenPayee(val id: PayeeId) : SearchPayeesAction

@JvmInline internal value class OpenRules(val payee: PayeeItem) : SearchPayeesAction
