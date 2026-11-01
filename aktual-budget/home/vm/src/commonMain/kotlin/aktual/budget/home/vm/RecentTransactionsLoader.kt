package aktual.budget.home.vm

import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.Amount
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest

@Inject
internal class RecentTransactionsLoader(private val transactionDao: TransactionDao) {
  fun observe(limit: Int): Flow<ImmutableList<RecentTransaction>> =
    transactionDao
      .observeChanges()
      .mapLatest {
        transactionDao
          .getPaged(limit = limit.toLong(), offset = 0)
          .rows
          .map { it.toRecentTransaction() }
          .toImmutableList()
      }
      .distinctUntilChanged()

  private fun TransactionRow.toRecentTransaction() =
    RecentTransaction(
      id = id,
      date = date,
      payee = transferAccountName ?: payeeName,
      transfer =
        when {
          transferAccountName == null -> null
          amount > 0 -> From
          else -> To
        },
      account = accountName,
      category = categoryName,
      amount = Amount(amount),
      needsCategory = needsCategory,
      isSplit = isParent,
    )
}
