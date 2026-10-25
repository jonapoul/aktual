package aktual.budget.payees.ui.search

import androidx.compose.runtime.Immutable

internal sealed interface SearchPayeesAction

internal data object NavBack : SearchPayeesAction

internal data object Retry : SearchPayeesAction

@JvmInline internal value class SetQuery(val query: String) : SearchPayeesAction

@Immutable
internal fun interface SearchPayeesActionHandler {
  operator fun invoke(action: SearchPayeesAction)
}
