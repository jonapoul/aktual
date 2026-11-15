package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId

/**
 * The writes of upstream's reconciliation flow, in desktop-client/src/accounts/reconciliation.ts
 */
interface Reconciler {
  /**
   * finishReconciliation(): locks the cleared transactions if their balance matches [target], then
   * marks the account reconciled either way, as AccountTransactions.tsx onDoneReconciling() does.
   * Returns whether it locked them.
   */
  suspend fun finish(account: AccountId, target: Amount): Boolean

  // A split's parts follow their parent, as updateTransaction() makes them
  suspend fun setCleared(id: TransactionId, cleared: Boolean)

  // unlockTransaction(), which leaves it cleared
  suspend fun unlock(id: TransactionId)

  /**
   * createReconciliationTransaction(): a cleared transaction for [difference], dated today and run
   * through the rules.
   */
  suspend fun createAdjustment(account: AccountId, difference: Amount, notes: String)
}
