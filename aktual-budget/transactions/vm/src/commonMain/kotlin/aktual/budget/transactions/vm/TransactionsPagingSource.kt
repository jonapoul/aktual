package aktual.budget.transactions.vm

import aktual.budget.db.GetFilterRows
import aktual.budget.db.GetSearchRows
import aktual.budget.db.dao.KnownBalance
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.dao.TransactionNotes
import aktual.budget.db.dao.TransactionRow
import aktual.budget.model.Amount
import aktual.budget.model.FilteredSpec
import aktual.budget.model.TagId
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionSearch
import aktual.budget.model.TransactionsSpec
import aktual.budget.model.notesContainTag
import aktual.budget.rules.domain.ConditionsMatcher
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.TransactionSearchMatcher
import aktual.core.utils.runCatchingCancellable
import androidx.paging.PagingSource
import androidx.paging.PagingState

internal class TransactionsPagingSource(
  private val transactionDao: TransactionDao,
  private val tagsDao: TagsDao,
  private val spec: TransactionsSpec,
  // Only applies to a single account's list
  private val hideReconciled: Boolean = false,
) : PagingSource<PageKey, Transaction>() {
  // A #tag match can't be expressed as a SQL offset query, so for tag-filtered specs we resolve the
  // full ordered id list once and page over it in memory. Cached for this source's lifetime - a new
  // source is created whenever the data is invalidated.
  private var filteredIds: FilteredIds? = null

  // The same for a report's drilled down list, whose conditions are matched in memory, and a
  // schedule's linked transactions
  private var filterIds: List<TransactionId>? = null

  // And for a searched account list, matched in memory too
  private var searchIds: FilteredIds? = null

  private val search =
    spec.search?.takeIf {
      !it.isEmpty && spec.tagSpec == AllTags && spec.categorySpec == AllCategories
    }

  private val accountId = (spec.accountSpec as? SpecificAccount)?.id

  override suspend fun load(params: LoadParams<PageKey>): LoadResult<PageKey, Transaction> =
    runCatchingCancellable(
      attempt = {
        // Keys are row offsets, not page numbers, since the first load is bigger than the rest. A
        // prepend's key is the offset its page ends at. A refresh can follow a change to the data,
        // so
        // it works the balance out again rather than carrying it
        val key = params.key?.offset ?: 0
        val known = params.key?.balance.takeIf { params !is Refresh }
        val isPrepend = params is Prepend
        val start = if (isPrepend) (key - params.loadSize).coerceAtLeast(0) else key
        val size = if (isPrepend) key - start else params.loadSize
        val offset = start.toLong()
        val limit = size.toLong()

        val filtered = spec.filtered
        val tagSpec = spec.tagSpec
        var top: Long? = null
        val transactions =
          when {
            // As category lists, a report's drilled down list has no balance and shows split parts
            filtered != null -> {
              val ids = filterIds ?: loadFilterIds(filtered).also { filterIds = it }
              transactionDao.getByIds(ids.page(offset, limit)).map {
                it.toTransaction(balance = null)
              }
            }

            // As tag lists, a search has no balance and shows splits with their matching parts
            search != null -> {
              val found = searchIds ?: loadSearchIds(search).also { searchIds = it }
              val rows = transactionDao.getByIds(found.ids.page(offset, limit))
              val children = childrenOf(rows)
              rows.map { row ->
                row.toTransaction(
                  balance = null,
                  children = children[row.id].orEmpty(),
                  shownChildren = found.children,
                )
              }
            }

            // A running balance means little over a subset of the account, so tag lists have none
            tagSpec is SpecificTag -> {
              val tagged = filteredIds ?: loadFilteredIds(tagSpec.id).also { filteredIds = it }
              val rows = transactionDao.getByIds(tagged.ids.page(offset, limit))
              val children = childrenOf(rows)
              rows.map { row ->
                row.toTransaction(
                  balance = null,
                  children = children[row.id].orEmpty(),
                  shownChildren = tagged.children,
                )
              }
            }

            else -> {
              val page = loadPage(limit, offset, known)
              top = page.top
              page.transactions
            }
          }

        val bottom = top?.let { it - transactions.sumOf { row -> row.amount.toLong() } }
        LoadResult.Page(
          data = transactions,
          prevKey = if (start > 0) PageKey(start, top?.let(KnownBalance::Below)) else null,
          nextKey =
            if (transactions.size < size) {
              null
            } else {
              PageKey(start + transactions.size, bottom?.let(KnownBalance::Top))
            },
        )
      },
      onFailure = { e -> LoadResult.Error(e) },
    )

  // A running balance means little over a subset of the account, so uncategorised lists have none
  private suspend fun loadPage(limit: Long, offset: Long, known: KnownBalance?): LoadedPage =
    when (val categorySpec = spec.categorySpec) {
      AllCategories -> {
        val page =
          when (val accountSpec = spec.accountSpec) {
            AllAccounts -> transactionDao.getPaged(limit, offset, known)
            is SpecificAccount ->
              transactionDao.getByAccountPaged(accountSpec.id, limit, offset, hideReconciled, known)
            is Group -> transactionDao.getByGroupPaged(accountSpec.group, limit, offset, known)
          }
        // With reconciled rows hidden, the rows between pages aren't all shown, so there's nothing
        // to carry
        LoadedPage(
          transactions = page.toTransactions(childrenOf(page.rows)),
          top = page.top.takeUnless { hideReconciled && spec.accountSpec is SpecificAccount },
        )
      }

      Uncategorised -> {
        val rows = transactionDao.getUncategorisedPaged(accountId, limit, offset)
        LoadedPage(rows.map { it.toTransaction(balance = null) })
      }

      // As uncategorised lists, a category's has no balance and shows split parts as rows
      is SpecificCategory -> {
        val rows =
          transactionDao.getByCategoryPaged(
            categorySpec.id,
            categorySpec.dates,
            accountId,
            limit,
            offset,
          )
        LoadedPage(rows.map { it.toTransaction(balance = null) })
      }
    }

  // top is the balance after the page's first row, when its list has a running balance
  private data class LoadedPage(val transactions: List<Transaction>, val top: Long? = null)

  // One query for all of the page's splits, and none when it has no splits
  private suspend fun childrenOf(
    rows: List<TransactionRow>,
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
    return when (val categorySpec = spec.categorySpec) {
      AllCategories -> {
        val rows =
          when (val accountSpec = spec.accountSpec) {
            AllAccounts -> transactionDao.getIdsAndNotes()
            is SpecificAccount -> transactionDao.getIdsAndNotesByAccount(accountSpec.id)
            is Group -> transactionDao.getIdsAndNotesByGroup(accountSpec.group)
          }
        groupSplits(rows, rows.filter(matches))
      }

      Uncategorised -> {
        val rows = transactionDao.getUncategorisedIdsAndNotes(accountId)
        FilteredIds(ids = rows.filter(matches).map { it.id })
      }

      is SpecificCategory -> {
        val rows =
          transactionDao.getIdsAndNotesByCategory(categorySpec.id, categorySpec.dates, accountId)
        FilteredIds(ids = rows.filter(matches).map { it.id })
      }
    }
  }

  private suspend fun loadFilterIds(filtered: FilteredSpec): List<TransactionId> =
    when (filtered) {
      is Conditions -> {
        val matcher = ConditionsMatcher(filtered.filter)
        transactionDao.getFilterRows().filter { matcher.matches(it.toFilterRow()) }.map { it.id }
      }
      is LinkedToSchedule -> transactionDao.getIdsBySchedule(filtered.id)
    }

  // A split takes one slot, as its parent, where its first matching child would be. Only the
  // children are matched, since a parent's payee and category come from them
  private suspend fun loadSearchIds(search: TransactionSearch): FilteredIds {
    val matcher = TransactionSearchMatcher(search)
    val rows =
      when (val accountSpec = spec.accountSpec) {
        AllAccounts -> transactionDao.getSearchRows(account = null, group = null)
        is SpecificAccount -> transactionDao.getSearchRows(accountSpec.id, group = null)
        is Group -> transactionDao.getSearchRows(account = null, accountSpec.group)
      }
    val ids = linkedSetOf<TransactionId>()
    val children = mutableSetOf<TransactionId>()
    val matching = rows.filter { row ->
      !(hideReconciled && row.reconciled == true) && matcher.matches(row.toFilterRow())
    }
    for (row in matching) {
      val parent = row.parent_id.takeIf { row.is_child == true }
      if (parent == null) {
        ids += row.id
      } else {
        ids += parent
        children += row.id
      }
    }
    return FilteredIds(ids = ids.toList(), children = children)
  }

  private fun List<TransactionId>.page(offset: Long, limit: Long): List<TransactionId> {
    val from = offset.toInt().coerceIn(0, size)
    val to = (from + limit.toInt()).coerceAtMost(size)
    return subList(from, to)
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
  private data class FilteredIds(
    val ids: List<TransactionId> = emptyList(),
    val children: Set<TransactionId> = emptySet(),
  )

  // Reload from the page nearest the scroll position, so a refresh keeps the user roughly in place
  override fun getRefreshKey(state: PagingState<PageKey, Transaction>): PageKey? =
    state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.let { PageKey(it.prevKey?.offset ?: 0) }
    }
}

// The row offset a page starts at, or a prepend's ends at, with the balance its neighbour knows
internal data class PageKey(val offset: Int, val balance: KnownBalance? = null)

private fun GetFilterRows.toFilterRow() =
  FilterRow(
    date = date,
    amount = Amount(amount),
    account = account,
    accountName = account_name,
    offBudget = off_budget == true,
    payee = payee,
    payeeName = payee_name,
    importedPayee = imported_payee?.value,
    category = category,
    categoryName = category_name,
    categoryGroup = category_group,
    categoryGroupName = category_group_name,
    notes = notes,
    cleared = cleared == true,
    reconciled = reconciled == true,
    isTransfer = transfer_id != null,
    isParent = is_parent == true,
  )

private fun GetSearchRows.toFilterRow() =
  FilterRow(
    date = date,
    amount = Amount(amount),
    account = account,
    accountName = account_name,
    offBudget = off_budget == true,
    payee = payee,
    payeeName = payee_name,
    transferAccountName = transfer_account_name,
    importedPayee = imported_payee?.value,
    category = category,
    categoryName = category_name,
    categoryGroup = category_group,
    categoryGroupName = category_group_name,
    notes = notes,
    cleared = cleared == true,
    reconciled = reconciled == true,
    isTransfer = transfer_id != null,
  )
