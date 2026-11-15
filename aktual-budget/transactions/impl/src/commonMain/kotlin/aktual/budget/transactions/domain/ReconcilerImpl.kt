package aktual.budget.transactions.domain

import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.core.utils.Calendar
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock

@Inject
@ContributesBinding(BudgetScope::class)
class ReconcilerImpl
internal constructor(
  private val writer: TransactionWriter,
  private val transactionDao: TransactionDao,
  private val rulesLoader: TransactionRulesLoader,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
  private val clock: Clock,
) : Reconciler {
  override suspend fun finish(account: AccountId, target: Amount): Boolean {
    val cleared = transactionDao.clearedBalance(account)
    val lock = cleared == target.toLong()
    val ids = if (lock) transactionDao.unreconciledClearedIds(account) else emptyList()
    writer.write {
      ids.forEach { update(TransactionUpdate(id = it, reconciled = true)) }
      updateAccount(AccountUpdate(id = account, lastReconciled = Patch.To(clock.now())))
    }
    log.i { "Finished reconciling $account, locked ${ids.size}" }
    return lock
  }

  override suspend fun setCleared(id: TransactionId, cleared: Boolean) {
    val ids = withParts(id)
    writer.write { ids.forEach { update(TransactionUpdate(id = it, cleared = cleared)) } }
  }

  override suspend fun unlock(id: TransactionId) {
    val ids = withParts(id)
    writer.write { ids.forEach { update(TransactionUpdate(id = it, reconciled = false)) } }
  }

  override suspend fun createAdjustment(account: AccountId, difference: Amount, notes: String) {
    val rules = rulesLoader.load()
    val transaction =
      RuleTransaction(
        account = account,
        date = calendar.today(),
        amount = difference,
        notes = notes,
        cleared = true,
      )
    val result = rules.run(transaction)
    if (result.tombstone) return
    val rows = result.toNewTransactions(newId = { uuidGenerator(::TransactionId) })
    val usedPayees: Set<PayeeId> = rows.mapNotNull { it.payee }.toSet()
    writer.write(runTransfers = true) {
      for ((payee, name) in rules.createdPayees) {
        if (payee in usedPayees) insertPayee(name, id = payee)
      }
      rows.forEach { insert(it) }
    }
    log.i { "Added reconciliation adjustment to $account" }
  }

  private suspend fun withParts(id: TransactionId): List<TransactionId> =
    listOf(id) + transactionDao.childrenOf(listOf(id))[id].orEmpty().map { it.id }
}
