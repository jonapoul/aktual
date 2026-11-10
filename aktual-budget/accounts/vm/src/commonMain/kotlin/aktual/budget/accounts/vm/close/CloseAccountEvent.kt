package aktual.budget.accounts.vm.close

import aktual.budget.accounts.domain.AccountUndo

sealed interface CloseAccountEvent {
  /**
   * Closed, or deleted if it had no transactions. [unlinkFailed] when bank sync couldn't be fully
   * unlinked. Undo doesn't relink it.
   */
  data class Closed(
    val name: String,
    val deleted: Boolean,
    val unlinkFailed: Boolean,
    val undo: AccountUndo,
  ) : CloseAccountEvent

  // Deleted with its transactions, which can't be undone
  data class Deleted(val name: String, val unlinkFailed: Boolean) : CloseAccountEvent
}
