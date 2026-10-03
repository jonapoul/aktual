package aktual.budget.transactions.vm

import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.TagId
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.notesContainTag
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException

internal class TransactionsPagingSource(
  private val transactionDao: TransactionDao,
  private val tagsDao: TagsDao,
  private val spec: TransactionsSpec,
) : PagingSource<Int, Transaction>() {
  // A #tag match can't be expressed as a SQL offset query, so for tag-filtered specs we resolve the
  // full ordered id list once and page over it in memory. Cached for this source's lifetime - a new
  // source is created whenever the data is invalidated.
  private var filteredIds: List<TransactionId>? = null

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> =
    try {
      // Start from page 0 if no key provided
      val page = params.key ?: 0
      val offset = (page * params.loadSize).toLong()
      val limit = params.loadSize.toLong()

      val rows =
        when (val tagSpec = spec.tagSpec) {
          AllTags -> {
            loadPage(limit, offset)
          }

          is SpecificTag -> {
            val ids = filteredIds ?: loadFilteredIds(tagSpec.id).also { filteredIds = it }
            val from = offset.toInt().coerceIn(0, ids.size)
            val to = (from + limit.toInt()).coerceAtMost(ids.size)
            transactionDao.getByIds(ids.subList(from, to))
          }
        }

      LoadResult.Page(
        data = rows.map { it.toTransaction() },
        prevKey = if (page > 0) page - 1 else null,
        nextKey = if (rows.size < params.loadSize) null else page + 1,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      LoadResult.Error(e)
    }

  private suspend fun loadPage(limit: Long, offset: Long): List<TransactionRow> =
    when (val accountSpec = spec.accountSpec) {
      AllAccounts -> transactionDao.getPaged(limit, offset)
      is SpecificAccount -> transactionDao.getByAccountPaged(accountSpec.id, limit, offset)
    }

  private suspend fun loadFilteredIds(id: TagId): List<TransactionId> {
    val tagName = tagsDao.getTag(id)?.tag ?: return emptyList()
    val rows =
      when (val accountSpec = spec.accountSpec) {
        AllAccounts -> transactionDao.getIdsAndNotes()
        is SpecificAccount -> transactionDao.getIdsAndNotesByAccount(accountSpec.id)
      }
    return rows.mapNotNull { row ->
      val notes = row.notes
      row.id.takeIf { notes != null && notesContainTag(notes, tagName) }
    }
  }

  override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
    // Try to find the page key of the closest item to the current scroll position
    // This ensures that when the data refreshes, the user stays at roughly the same position
    return state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
  }
}
