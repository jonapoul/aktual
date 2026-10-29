package aktual.budget.transactions.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionsEvent {
  data object AccountDeleted : TransactionsEvent

  // Deleted, but its GoCardless bank login couldn't be removed from the server
  data object AccountDeletedBankLoginKept : TransactionsEvent

  data object DeleteAccountFailed : TransactionsEvent

  data object RenameAccountFailed : TransactionsEvent
}
