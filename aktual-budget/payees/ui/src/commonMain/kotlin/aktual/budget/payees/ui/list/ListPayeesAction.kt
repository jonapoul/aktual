package aktual.budget.payees.ui.list

import androidx.compose.runtime.Immutable

internal sealed interface ListPayeesAction

internal data object Retry : ListPayeesAction

@Immutable
internal fun interface ListPayeesActionHandler {
  operator fun invoke(action: ListPayeesAction)
}
