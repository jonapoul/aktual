package aktual.budget.banksync.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey.PerAccount.NewTransactionsSeen
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
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

@Inject
@ContributesBinding(BudgetScope::class, binding<NewTransactions>())
class NewTransactionsImpl
internal constructor(
  private val transactionDao: TransactionDao,
  private val preferences: PreferencesDao,
  private val syncController: BudgetSyncController,
  private val clock: Clock,
) : NewTransactions {
  override val counts: Flow<Map<AccountId, Int>> = transactionDao.observeNewImportedCounts()

  // An import's sort orders count down from now, so a big one can reach back past when the
  // account was last opened. Moving that back keeps all of it new, and starts tracking an account
  // that never was
  override suspend fun imported(account: AccountId, ids: List<TransactionId>) {
    val oldest = transactionDao.oldestImportedSortOrder(ids) ?: return
    val seen = preferences[NewTransactionsSeen(account)]?.toDoubleOrNull()
    if (seen == null || seen >= oldest) setSeen(account, oldest.toLong() - 1)
  }

  override suspend fun markSeen(account: AccountId) =
    setSeen(account, clock.now().toEpochMilliseconds())

  private suspend fun setSeen(account: AccountId, millis: Long) =
    syncController.syncChanges(
      localChange(
        dataset = PREFERENCES,
        row = NewTransactionsSeen(account).key,
        column = "value",
        value = millis.toString(),
      ),
    )
}
