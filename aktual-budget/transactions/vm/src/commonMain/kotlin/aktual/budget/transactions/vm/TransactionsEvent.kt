package aktual.budget.transactions.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionsEvent {
  data object AccountDeleted : TransactionsEvent

  data object DeleteAccountFailed : TransactionsEvent
}
