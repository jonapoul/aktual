package aktual.budget.transactions.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.core.UuidGenerator
import aktual.di.BudgetScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import logcat.logcat

/**
 * Writes transactions, payees and account sync fields, like the db.insertTransaction() /
 * updateTransaction() / deleteTransaction() / insertPayee() / update('accounts') calls that
 * packages/loot-core/src/server/transactions/index.ts batchUpdateTransactions() and the bank sync
 * in accounts/sync.ts make inside batchMessages(). Everything goes through [BudgetSyncController],
 * which also applies the changes to the local database.
 *
 * Transfers, rules and category learning aren't run.
 */
@Inject
@SingleIn(BudgetScope::class)
class TransactionWriter(
  private val syncController: BudgetSyncController,
  private val accountDao: AccountDao,
  private val categoryDao: CategoryDao,
  private val payeeDao: PayeeDao,
  private val transactionDao: TransactionDao,
  private val uuidGenerator: UuidGenerator,
  private val clock: Clock,
) {
  private val mutex = Mutex()

  /**
   * Runs [block] to collect changes, then sends them all in one syncChanges() call, unless there
   * are none. Returns whatever [block] returns, e.g. the IDs it created. Calls run one at a time,
   * so each batch sees what the last one wrote. Don't call it again from inside [block].
   */
  suspend fun <R> write(block: suspend TransactionBatch.() -> R): R = mutex.withLock {
    val batch =
      TransactionBatch(
        accountDao = accountDao,
        categoryDao = categoryDao,
        payeeDao = payeeDao,
        transactionDao = transactionDao,
        uuidGenerator = uuidGenerator,
        clock = clock,
      )
    val result = batch.block()
    val changes = batch.changes()
    if (changes.isNotEmpty()) {
      syncController.syncChanges(changes)
      logcat.i { "Wrote ${changes.size} transaction changes" }
    }
    result
  }
}
