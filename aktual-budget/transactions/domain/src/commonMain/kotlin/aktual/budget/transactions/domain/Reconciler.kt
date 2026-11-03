package aktual.budget.transactions.domain

import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.core.Calendar
import aktual.core.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import logcat.logcat

/**
 * The writes of upstream's reconciliation flow, in desktop-client/src/accounts/reconciliation.ts
 */
@Inject
class Reconciler
internal constructor(
  private val writer: TransactionWriter,
  private val transactionDao: TransactionDao,
  private val rulesLoader: TransactionRulesLoader,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
  private val clock: Clock,
) {
  /**
   * finishReconciliation(): locks the cleared transactions if their balance matches [target], then
   * marks the account reconciled either way, as AccountTransactions.tsx onDoneReconciling() does.
   * Returns whether it locked them.
   */
  suspend fun finish(account: AccountId, target: Amount): Boolean {
    val cleared = transactionDao.clearedBalance(account)
    val lock = cleared == target.toLong()
    val ids = if (lock) transactionDao.unreconciledClearedIds(account) else []
    writer.write {
      ids.forEach { update(TransactionUpdate(id = it, reconciled = true)) }
      updateAccount(AccountUpdate(id = account, lastReconciled = Patch.To(clock.now())))
    }
    logcat.i { "Finished reconciling $account, locked ${ids.size}" }
    return lock
  }

  // A split's parts follow their parent, as updateTransaction() makes them
  suspend fun setCleared(id: TransactionId, cleared: Boolean) {
    val ids = withParts(id)
    writer.write { ids.forEach { update(TransactionUpdate(id = it, cleared = cleared)) } }
  }

  // unlockTransaction(), which leaves it cleared
  suspend fun unlock(id: TransactionId) {
    val ids = withParts(id)
    writer.write { ids.forEach { update(TransactionUpdate(id = it, reconciled = false)) } }
  }

  /**
   * createReconciliationTransaction(): a cleared transaction for [difference], dated today and run
   * through the rules.
   */
  suspend fun createAdjustment(account: AccountId, difference: Amount, notes: String) {
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
    logcat.i { "Added reconciliation adjustment to $account" }
  }

  private suspend fun withParts(id: TransactionId): List<TransactionId> =
    [id] + transactionDao.childrenOf([id])[id].orEmpty().map { it.id }
}
