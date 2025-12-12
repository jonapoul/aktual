package aktual.budget.transactions.vm

import aktual.budget.db.dao.TransactionsDao
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException

internal class TransactionsPagingSource(
  private val dao: TransactionsDao,
  private val accountId: AccountId?,
) : PagingSource<Int, TransactionId>() {

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionId> {
    return try {
      // Start from page 0 if no key provided
      val page = params.key ?: 0
      val offset = (page * params.loadSize).toLong()
      val limit = params.loadSize.toLong()

      // Load transaction IDs based on account filter
      val transactionIds = if (accountId != null) {
        dao.getIdsByAccountPaged(accountId, limit, offset)
      } else {
        dao.getIdsPaged(limit, offset)
      }

      LoadResult.Page(
        data = transactionIds,
        prevKey = if (page > 0) page - 1 else null,
        nextKey = if (transactionIds.isEmpty()) null else page + 1,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<Int, TransactionId>): Int? {
    // Try to find the page key of the closest item to the current scroll position
    // This ensures that when the data refreshes, the user stays at roughly the same position
    return state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
  }
}
