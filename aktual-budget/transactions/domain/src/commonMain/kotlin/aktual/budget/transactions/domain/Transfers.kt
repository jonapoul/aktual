package aktual.budget.transactions.domain

import aktual.budget.db.Transactions
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import logcat.logcat

/**
 * packages/loot-core/src/server/transactions/transfer.ts: keeps the other side of each transfer in
 * step with the transactions a batch wrote. As batchUpdateTransactions() does, it runs once those
 * changes are in the database, and collects its own in [batch]. Nothing it writes is read back, so
 * every transaction is seen as the first batch left it.
 *
 * Unlike upstream, values that already match aren't written again, and rules aren't run on a new
 * counterpart.
 */
internal class Transfers(
  private val batch: TransactionBatch,
  private val accountDao: AccountDao,
  private val payeeDao: PayeeDao,
  private val transactionDao: TransactionDao,
) {
  suspend fun run(touched: TouchedTransactions) {
    for (id in touched.added) transactionDao.row(id)?.let { onInsert(it) }
    for (id in touched.updated) transactionDao.row(id)?.let { onUpdate(it) }
    for (id in touched.deleted) transactionDao.row(id)?.let { onDelete(it) }
  }

  private suspend fun onInsert(t: Transactions) {
    val account = transferAccount(t) ?: return
    addTransfer(t, account)
  }

  private suspend fun onUpdate(t: Transactions) {
    val account = transferAccount(t)
    val linked = t.transferred_id
    when {
      // Upstream unlinks every parent it updates. One with no transfer has nothing to unlink
      t.isParent == true -> if (linked != null) removeTransfer(t, linked)
      account != null && linked == null -> addTransfer(t, account)
      account == null && linked != null -> removeTransfer(t, linked)
      account != null && linked != null -> updateTransfer(t, linked, account)
    }
  }

  private suspend fun onDelete(t: Transactions) {
    val linked = t.transferred_id ?: return
    removeTransfer(t, linked)
  }

  private suspend fun addTransfer(t: Transactions, to: AccountId) {
    // A split's transfers are made by its parts, so the other account gets their amounts and not
    // the total
    if (t.isParent == true) return
    val from = t.acct
    val date = t.date
    val payee = from?.let { payeeDao.transferPayee(it) }
    if (from == null || date == null || payee == null) {
      logcat.w { "Can't transfer ${t.id} from $from on $date" }
      return
    }

    val counterpart =
      NewTransaction(
        account = to,
        date = date,
        amount = -(t.amount ?: Zero),
        payee = payee,
        notes = t.notes?.ifEmpty { null },
        transferId = t.id,
        schedule = t.schedule,
        cleared = false,
      )
    val id = batch.insert(counterpart)
    batch.update(TransactionUpdate(id = t.id, transferId = Patch.To(id)))
    clearCategory(t, to, linked = null)
  }

  private suspend fun removeTransfer(t: Transactions, linked: TransactionId) {
    // It's gone already if both sides were deleted together
    val other = transactionDao.row(linked)?.takeIf { it.tombstone != true }
    if (other?.isChild == true) {
      // Deleting a part would leave its split not adding up, so it becomes an ordinary one
      val unlinked =
        TransactionUpdate(id = linked, transferId = Patch.To(null), payee = Patch.To(null))
      batch.update(unlinked)
    } else if (other != null) {
      batch.delete(linked)
    }
    batch.update(TransactionUpdate(id = t.id, transferId = Patch.To(null)))
  }

  private suspend fun updateTransfer(t: Transactions, linked: TransactionId, to: AccountId) {
    val payee = t.acct?.let { payeeDao.transferPayee(it) }
    if (payee == null) {
      logcat.w { "No transfer payee for ${t.acct}, the account of ${t.id}" }
      return
    }

    val other = transactionDao.row(linked)
    val amount = -(t.amount ?: Zero)
    val update =
      TransactionUpdate(
        id = linked,
        account = to.takeIf { it != other?.acct },
        // The payee follows this side into whichever account it's been moved to
        payee = changed<PayeeId?>(payee, other?.description),
        notes = changed(t.notes, other?.notes),
        amount = amount.takeIf { it != other?.amount },
        schedule = changed(t.schedule, other?.schedule),
      )
    batch.update(update)
    clearCategory(t, to, linked)
  }

  // A transfer between two on budget accounts, or two off budget ones, moves no money in or out
  // of the budget, so neither side has a category
  private suspend fun clearCategory(t: Transactions, to: AccountId, linked: TransactionId?) {
    val from = t.acct ?: return
    if (isOffBudget(from) != isOffBudget(to)) return
    if (t.category != null) {
      batch.update(TransactionUpdate(id = t.id, category = Patch.To(null)))
    }
    if (linked != null && transactionDao.row(linked)?.category != null) {
      batch.update(TransactionUpdate(id = linked, category = Patch.To(null)))
    }
  }

  private fun <T> changed(value: T, stored: T): Patch<T> =
    if (value == stored) Keep else Patch.To(value)

  private suspend fun transferAccount(t: Transactions): AccountId? =
    t.description?.let { payeeDao.transferAccount(it) }

  private suspend fun isOffBudget(account: AccountId): Boolean =
    accountDao[account]?.offbudget == true
}
