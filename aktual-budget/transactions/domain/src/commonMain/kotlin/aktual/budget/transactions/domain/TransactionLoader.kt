package aktual.budget.transactions.domain

import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.dao.TransactionDetail
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.TransactionId
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// A transaction with its split's parts, if it's a split parent, and its account's balance after it
data class LoadedTransaction(
  val detail: TransactionDetail,
  val children: List<TransactionRow>,
  val balanceAfter: Long,
)

/**
 * Loads one transaction for its detail screen. As upstream's TransactionEdit, which loads it with
 * splits: 'grouped', a split child loads as its whole split.
 */
@Inject
class TransactionLoader(private val transactionDao: TransactionDao) {
  // Null if there's no live transaction with this ID
  suspend fun load(id: TransactionId): LoadedTransaction? {
    val opened = transactionDao.detail(id) ?: return null
    val detail = opened.parent?.let { transactionDao.detail(it) } ?: opened
    val row = detail.row
    val children =
      if (row.isParent) transactionDao.childrenOf(listOf(row.id))[row.id].orEmpty() else emptyList()
    return LoadedTransaction(detail, children, transactionDao.balanceAfter(row.id))
  }

  // Reloads whenever a table behind the transactions view changes, starting with the current state
  fun observe(id: TransactionId): Flow<LoadedTransaction?> =
    transactionDao.observeChanges().map { load(id) }.distinctUntilChanged()
}
