package actual.budget.transactions.ui

import actual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
internal sealed interface TransactionsAction {
  data object NavBack : TransactionsAction
  data class ExpandGroup(val group: LocalDate, val isExpanded: Boolean) : TransactionsAction
  data class CheckItem(val id: TransactionId, val isChecked: Boolean) : TransactionsAction
}
