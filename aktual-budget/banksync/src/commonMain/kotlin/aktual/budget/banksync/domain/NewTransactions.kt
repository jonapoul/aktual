package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import kotlinx.coroutines.flow.Flow

/**
 * Bank-imported transactions each account got since it was last opened, on any device. Each account
 * keeps the time it was last opened in a synced pref, and a transaction is new when its sort order,
 * the time it was created, is later.
 */
interface NewTransactions {
  // Accounts with nothing new are left out
  val counts: Flow<Map<AccountId, Int>>

  // Called after a bank sync adds transactions to an account
  suspend fun imported(account: AccountId, ids: List<TransactionId>)

  suspend fun markSeen(account: AccountId)
}
