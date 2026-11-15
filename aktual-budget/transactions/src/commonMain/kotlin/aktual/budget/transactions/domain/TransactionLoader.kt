package aktual.budget.transactions.domain

import aktual.budget.db.dao.TransactionDetail
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import kotlinx.coroutines.flow.Flow

// A transaction with its split's parts, if it's a split parent, and its account's balance after it
data class LoadedTransaction(
  val detail: TransactionDetail,
  val children: List<TransactionRow>,
  val balanceAfter: Long,
  // The children again, as their editor holds them
  val parts: List<SplitPart> = emptyList(),
  // Whether it, or a part of it, is a transfer whose other side is reconciled
  val hasReconciledTransfer: Boolean = false,
) {
  val fields: TransactionFields
    get() =
      TransactionFields(
        account = detail.account,
        date = detail.row.date,
        amount = Amount(detail.row.amount),
        payee = detail.payee,
        category = detail.category,
        notes = detail.row.notes,
        cleared = detail.cleared,
        reconciled = detail.reconciled,
        schedule = detail.schedule,
      )

  val draft: TransactionDraft
    get() = TransactionDraft(fields, parts)
}

/**
 * Loads one transaction for its detail screen. As upstream's TransactionEdit, which loads it with
 * splits: 'grouped', a split child loads as its whole split.
 */
interface TransactionLoader {
  // Null if there's no live transaction with this ID
  suspend fun load(id: TransactionId): LoadedTransaction?

  // Reloads whenever a table behind the transactions view changes, starting with the current state
  fun observe(id: TransactionId): Flow<LoadedTransaction?>
}
