package aktual.budget.transactions.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionsEvent {
  data object AccountDeleted : TransactionsEvent

  // Deleted, but its GoCardless bank login couldn't be removed from the server
  data object AccountDeletedBankLoginKept : TransactionsEvent

  data object DeleteAccountFailed : TransactionsEvent

  data object ReconcileFailed : TransactionsEvent

  @JvmInline value class FilterNameTaken(val name: String) : TransactionsEvent

  // Another saved filter, named [name], already has the same conditions
  @JvmInline value class FilterConditionsTaken(val name: String) : TransactionsEvent

  data object SaveFilterFailed : TransactionsEvent
}
