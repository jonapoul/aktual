package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Downloads transactions for the open budget's linked accounts and imports them: accountsBankSync()
 * and simpleFinBatchSync() in packages/loot-core/src/server/accounts/app.ts. Each account is synced
 * in turn, recording when it last synced or why it failed, and the changes go through
 * TransactionWriter like any others.
 *
 * Several SimpleFIN accounts are downloaded in one request, as the desktop client does when syncing
 * everything, since SimpleFIN limits how often it's asked for data.
 */
interface BankSyncController {
  val progress: StateFlow<BankSyncProgress>

  val finished: SharedFlow<List<BankSyncResult>>

  fun start(accounts: Set<AccountId> = emptySet()): Boolean

  suspend fun sync(accounts: Set<AccountId> = emptySet()): List<BankSyncResult>
}
