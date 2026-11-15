package aktual.budget.banksync.domain

import aktual.budget.db.dao.DatabaseTables.PREFERENCES
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.SyncedPrefKey.PerAccount.NewTransactionsSeen
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSync
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(BudgetScope::class, binding<NewTransactions>())
class NewTransactionsImpl
internal constructor(
  private val transactionDao: TransactionDao,
  private val preferences: PreferencesDao,
  private val budgetSync: BudgetSync,
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
    budgetSync(
      localChange(
        dataset = PREFERENCES,
        row = NewTransactionsSeen(account).key,
        column = "value",
        value = millis.toString(),
      ),
    )
}
