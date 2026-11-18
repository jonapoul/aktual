package aktual.budget.transactions.domain

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.rules.domain.CategoryLearner
import aktual.budget.utils.BudgetSync
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Inject
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class)
class TransactionWriterImpl(
  private val budgetSync: BudgetSync,
  private val accountDao: AccountDao,
  private val categoryDao: CategoryDao,
  private val payeeDao: PayeeDao,
  private val transactionDao: TransactionDao,
  private val categoryLearner: CategoryLearner,
  private val uuidGenerator: UuidGenerator,
  private val clock: Clock,
) : TransactionWriter {
  private val mutex = Mutex()

  override suspend fun <R> write(
    runTransfers: Boolean,
    syncTransferDates: Boolean,
    learnCategories: Boolean,
    block: suspend TransactionBatch.() -> R,
  ): R = mutex.withLock {
    val batch = newBatch()
    val result = batch.block()
    send(batch)
    if (runTransfers) {
      val transfers = newBatch()
      Transfers(transfers, accountDao, payeeDao, transactionDao, syncTransferDates)
        .run(batch.touched())
      send(transfers)
    }
    if (learnCategories) categoryLearner.learn(batch.categorised())
    result
  }

  private fun newBatch() =
    TransactionBatchImpl(
      accountDao = accountDao,
      categoryDao = categoryDao,
      payeeDao = payeeDao,
      transactionDao = transactionDao,
      uuidGenerator = uuidGenerator,
      clock = clock,
    )

  private suspend fun send(batch: TransactionBatchImpl) {
    val changes = batch.changes()
    if (changes.isEmpty()) return
    budgetSync(changes)
    log.i { "Wrote ${changes.size} transaction changes" }
  }
}
