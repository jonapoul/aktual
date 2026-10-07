package aktual.budget.transactions.vm

import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.dao.TransactionNotes
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.AccountSpec
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
  private var filteredIds: FilteredIds? = null

  private val accountId = (spec.accountSpec as? AccountSpec.SpecificAccount)?.id

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> =
    try {
      // Start from page 0 if no key provided
      val page = params.key ?: 0
      val offset = (page * params.loadSize).toLong()
      val limit = params.loadSize.toLong()

      val transactions =
        when (val tagSpec = spec.tagSpec) {
          AllTags -> {
            loadPage(limit, offset)
          }

          // A running balance means little over a subset of the account, so tag lists have none
          is SpecificTag -> {
            val filtered = filteredIds ?: loadFilteredIds(tagSpec.id).also { filteredIds = it }
            val ids = filtered.ids
            val from = offset.toInt().coerceIn(0, ids.size)
            val to = (from + limit.toInt()).coerceAtMost(ids.size)
            val rows = transactionDao.getByIds(ids.subList(from, to))
            val children = childrenOf(rows)
            rows.map { row ->
              row.toTransaction(
                balance = null,
                children = children[row.id].orEmpty(),
                shownChildren = filtered.children,
              )
            }
          }
        }

      LoadResult.Page(
        data = transactions,
        prevKey = if (page > 0) page - 1 else null,
        nextKey = if (transactions.size < params.loadSize) null else page + 1,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      LoadResult.Error(e)
    }

  // A running balance means little over a subset of the account, so uncategorised lists have none
  private suspend fun loadPage(limit: Long, offset: Long): List<Transaction> =
    when (spec.categorySpec) {
      AllCategories -> {
        val page =
          when (val accountSpec = spec.accountSpec) {
            AllAccounts -> transactionDao.getPaged(limit, offset)
            is SpecificAccount -> transactionDao.getByAccountPaged(accountSpec.id, limit, offset)
          }
        page.toTransactions(childrenOf(page.rows))
      }

      Uncategorised -> {
        transactionDao.getUncategorisedPaged(accountId, limit, offset).map {
          it.toTransaction(balance = null)
        }
      }
    }

  // One query for all of the page's splits, and none when it has no splits
  private suspend fun childrenOf(
    rows: List<TransactionRow>
  ): Map<TransactionId, List<TransactionRow>> {
    val parents = rows.filter { it.isParent }.map { it.id }
    return if (parents.isEmpty()) emptyMap() else transactionDao.childrenOf(parents)
  }

  private suspend fun loadFilteredIds(id: TagId): FilteredIds {
    val tagName = tagsDao.getTag(id)?.tag ?: return FilteredIds()
    val matches = { row: TransactionNotes ->
      val notes = row.notes
      notes != null && notesContainTag(notes, tagName)
    }
    return when (spec.categorySpec) {
      AllCategories -> {
        val rows =
          when (val accountSpec = spec.accountSpec) {
            AllAccounts -> transactionDao.getIdsAndNotes()
            is SpecificAccount -> transactionDao.getIdsAndNotesByAccount(accountSpec.id)
          }
        groupSplits(rows, rows.filter(matches))
      }

      Uncategorised -> {
        val rows = transactionDao.getUncategorisedIdsAndNotes(accountId)
        FilteredIds(ids = rows.filter(matches).map { it.id })
      }
    }
  }

  // A split takes one slot, as its parent, if the parent or any of its children match
  private fun groupSplits(
    rows: List<TransactionNotes>,
    matching: List<TransactionNotes>,
  ): FilteredIds {
    val (topLevel, children) = matching.partition { it.parent == null }
    val ids = topLevel.mapTo(mutableSetOf()) { it.id }
    children.mapNotNullTo(ids) { it.parent }
    return FilteredIds(
      ids = rows.filter { it.parent == null && it.id in ids }.map { it.id },
      children = children.mapTo(mutableSetOf()) { it.id },
    )
  }

  // The ids to page over, in list order, with the split children to show under them
  private class FilteredIds(
    val ids: List<TransactionId> = emptyList(),
    val children: Set<TransactionId> = emptySet(),
  )

  override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
    // Try to find the page key of the closest item to the current scroll position
    // This ensures that when the data refreshes, the user stays at roughly the same position
    return state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
  }
}
