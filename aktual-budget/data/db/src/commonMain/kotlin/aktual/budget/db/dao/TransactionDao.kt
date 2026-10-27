package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetFilterRows
import aktual.budget.db.NotesContainingHash
import aktual.budget.db.Transactions
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull

// parent is set on split children only
data class TransactionNotes(val id: TransactionId, val notes: String?, val parent: TransactionId?)

data class TransactionRow(
  val id: TransactionId,
  val date: LocalDate,
  val accountName: String?,
  val payeeName: String?,
  val transferAccountName: String?,
  val notes: String?,
  val categoryName: String?,
  val amount: Long,
  val isParent: Boolean,
  val isChild: Boolean?,
  val needsCategory: Boolean,
  val offBudget: Boolean,
  val isTransfer: Boolean,
  // What a split parent's amount has over the sum of its parts, if they don't add up
  val splitDifference: Long? = null,
  // Null when not from a schedule
  val scheduleRecurring: Boolean? = null,
)

// One transaction as its detail screen shows it. parent is set on split children only
data class TransactionDetail(
  val row: TransactionRow,
  val cleared: Boolean,
  val reconciled: Boolean,
  val parent: TransactionId?,
  val account: AccountId?,
  val payee: PayeeId?,
  val category: CategoryId?,
)

// What a split child's editor needs on top of its row
data class ChildFields(val payee: PayeeId?, val category: CategoryId?, val sortOrder: Long?)

// One page of the list, with the balance after each of its rows
data class TransactionPage(val rows: List<TransactionRow>, val balances: List<Long>)

// An account's totals of its cleared and uncleared transactions
@Immutable data class ClearedBalances(val cleared: Long, val uncleared: Long)

@Suppress("TooManyFunctions") // One per query the transaction screens and writer need
@Inject
class TransactionDao(database: BudgetDatabase) {
  private val queries = database.transactionsQueries

  suspend fun getPaged(limit: Long, offset: Long): TransactionPage = queries.withResult {
    pageOf(
      rows = getPaged(limit, offset, ::transactionRow).awaitAsList(),
      topBalance = balanceFromOffset(offset).awaitAsOne(),
    )
  }

  // Hiding reconciled rows leaves gaps in the page, so each row's balance is looked up instead of
  // walked down from the first
  suspend fun getByAccountPaged(
    account: AccountId,
    limit: Long,
    offset: Long,
    hideReconciled: Boolean = false,
  ): TransactionPage = queries.withResult {
    val rows =
      getByAccountPaged(account, hideReconciled.toLong(), limit, offset, ::transactionRow)
        .awaitAsList()
    if (hideReconciled) {
      val balances =
        rows
          .chunked(MAX_BIND_ARGS)
          .flatMap { chunk -> balancesAfterByAccount(account, chunk.map { it.id }).awaitAsList() }
          .associate { it.id to it.balance }
      TransactionPage(rows, rows.map { balances[it.id] ?: 0L })
    } else {
      pageOf(rows, topBalance = balanceFromOffsetByAccount(account, offset).awaitAsOne())
    }
  }

  suspend fun getByGroupPaged(
    group: AccountGroup,
    limit: Long,
    offset: Long,
  ): TransactionPage = queries.withResult {
    val closed = group == Closed
    val offBudget = group.offBudget
    pageOf(
      rows = getByGroupPaged(closed, offBudget, limit, offset, ::transactionRow).awaitAsList(),
      topBalance = balanceFromOffsetByGroup(closed, offBudget, offset).awaitAsOne(),
    )
  }

  // Uncategorised transactions are a subset of their accounts, so the page has no balance
  suspend fun getUncategorisedPaged(
    account: AccountId?,
    limit: Long,
    offset: Long,
  ): List<TransactionRow> = queries.withResult {
    getUncategorisedPaged(account, limit, offset, ::transactionRow).awaitAsList()
  }

  // A category's transactions are a subset of their accounts, so the page has no balance
  suspend fun getByCategoryPaged(
    category: CategoryId,
    dates: ClosedRange<LocalDate>,
    account: AccountId?,
    limit: Long,
    offset: Long,
  ): List<TransactionRow> = queries.withResult {
    getByCategoryPaged(
        category = category,
        fromDate = dates.start,
        toDate = dates.endInclusive,
        account = account,
        limit = limit,
        offset = offset,
        mapper = ::transactionRow,
      )
      .awaitAsList()
  }

  fun observeUncategorisedCount(): Flow<Long> =
    queries.countUncategorised().asFlow().map { it.awaitAsOne() }.distinctUntilChanged()

  fun observeNewImportedCounts(): Flow<Map<AccountId, Int>> =
    queries
      .countNewImported()
      .asFlow()
      .map { query ->
        query.awaitAsList().associate { row -> requireNotNull(row.account) to row.count.toInt() }
      }
      .distinctUntilChanged()

  suspend fun oldestImportedSortOrder(ids: List<TransactionId>): Double? = queries.withResult {
    ids
      .chunked(MAX_BIND_ARGS)
      .mapNotNull { chunk -> oldestImportedSortOrder(chunk).awaitAsOne().oldest }
      .minOrNull()
  }

  // Current balance of every transaction, or of one account's
  fun observeBalance(account: AccountId? = null): Flow<Long> {
    val query =
      if (account == null) {
        queries.balanceFromOffset(offset = 0)
      } else {
        queries.balanceFromOffsetByAccount(account, offset = 0)
      }
    return query.asFlow().map { it.awaitAsOne() }.distinctUntilChanged()
  }

  fun observeBalance(group: AccountGroup): Flow<Long> =
    queries
      .balanceFromOffsetByGroup(group == Closed, group.offBudget, offset = 0)
      .asFlow()
      .map { it.awaitAsOne() }
      .distinctUntilChanged()

  fun observeClearedBalances(account: AccountId): Flow<ClearedBalances> =
    queries
      .clearedBalancesByAccount(account) { cleared, uncleared ->
        ClearedBalances(cleared, uncleared)
      }
      .asFlow()
      .map { it.awaitAsOne() }
      .distinctUntilChanged()

  // Rows come back in the order of the given IDs
  suspend fun getByIds(ids: List<TransactionId>): List<TransactionRow> = queries.withResult {
    val rows = getByIds(ids, ::transactionRow).awaitAsList().associateBy { it.id }
    ids.mapNotNull(rows::get)
  }

  suspend fun detail(id: TransactionId): TransactionDetail? = queries.withResult {
    getDetail(id, ::transactionDetail).awaitAsOneOrNull()
  }

  // The account's balance after this transaction, as the list's running balance shows it
  suspend fun balanceAfter(id: TransactionId): Long = queries.withResult {
    balanceAfter(id).awaitAsOne()
  }

  suspend fun getIdsAndNotes(): List<TransactionNotes> = queries.withResult {
    getIdsAndNotes(::transactionNotes).awaitAsList()
  }

  suspend fun getIdsAndNotesByAccount(account: AccountId): List<TransactionNotes> =
    queries.withResult {
      getIdsAndNotesByAccount(account, ::transactionNotes).awaitAsList()
    }

  suspend fun getIdsAndNotesByGroup(group: AccountGroup): List<TransactionNotes> =
    queries.withResult {
      getIdsAndNotesByGroup(group == Closed, group.offBudget, ::transactionNotes).awaitAsList()
    }

  suspend fun getUncategorisedIdsAndNotes(account: AccountId?): List<TransactionNotes> =
    queries.withResult {
      getUncategorisedIdsAndNotes(account, ::transactionNotes).awaitAsList()
    }

  suspend fun getFilterRows(): List<GetFilterRows> = queries.withResult {
    getFilterRows().awaitAsList()
  }

  suspend fun getIdsAndNotesByCategory(
    category: CategoryId,
    dates: ClosedRange<LocalDate>,
    account: AccountId?,
  ): List<TransactionNotes> = queries.withResult {
    getIdsAndNotesByCategory(
        category = category,
        fromDate = dates.start,
        toDate = dates.endInclusive,
        account = account,
        mapper = ::transactionNotes,
      )
      .awaitAsList()
  }

  suspend fun getNotesContainingHash(): List<String> = queries.withResult {
    notesContainingHash().awaitAsList().mapNotNull(NotesContainingHash::notes)
  }

  suspend fun row(id: TransactionId): Transactions? = queries.withResult {
    getRowById(id).awaitAsOneOrNull()
  }

  // Each live transaction in the account, split parts included, with the other side of its transfer
  suspend fun accountTransfers(account: AccountId): Map<TransactionId, TransactionId?> =
    queries.withResult {
      getAccountTransfers(account).awaitAsList().associate { it.id to it.transferred_id }
    }

  // Every child of these split parents, deleted or not, as upstream's idsWithChildren()
  suspend fun childIds(parents: Collection<TransactionId>): List<TransactionId> =
    queries.withResult {
      parents.chunked(MAX_BIND_ARGS).flatMap { chunk -> getChildIds(chunk).awaitAsList() }
    }

  // The live children of these split parents, each parent's in the order they were entered
  suspend fun childrenOf(
    parents: Collection<TransactionId>,
  ): Map<TransactionId, List<TransactionRow>> = queries.withResult {
    parents
      .chunked(MAX_BIND_ARGS)
      .flatMap { chunk -> getChildrenOf(chunk, ::childRow).awaitAsList() }
      .groupBy({ it.first }, { it.second })
  }

  suspend fun childFields(parent: TransactionId): Map<TransactionId, ChildFields> =
    queries.withResult {
      getChildFields(parent).awaitAsList().associate { child ->
        child.id to ChildFields(child.payee, child.category, child.sortOrder?.toLong())
      }
    }

  suspend fun hasReconciledTransfer(id: TransactionId): Boolean = queries.withResult {
    hasReconciledTransfer(id).awaitAsOne()
  }

  // Emits whenever a table behind the transactions view is written to
  fun observeChanges(): Flow<Unit> = queries.getIdsCount().asFlow().map {}

  suspend fun insert(
    id: String,
    account: String,
    category: String?,
    payee: String,
    date: LocalDate,
    notes: String? = null,
    amount: Double = 0.0,
    isParent: Boolean = false,
    parent: String? = null,
    schedule: String? = null,
    error: JsonObject? = null,
    transferId: String? = null,
  ) = queries.withoutResult {
    insert(
      Transactions(
        id = TransactionId(id),
        isParent = isParent,
        isChild = parent != null,
        acct = AccountId(account),
        category = category?.let(::CategoryId),
        amount = Amount(amount),
        description = PayeeId(payee),
        notes = notes,
        date = date,
        financial_id = null,
        type = null,
        location = null,
        error = error,
        imported_description = null,
        starting_balance_flag = null,
        transferred_id = transferId?.let(::TransactionId),
        sort_order = date.toEpochDays().days.inWholeMilliseconds.toDouble(),
        tombstone = false,
        cleared = null,
        pending = null,
        parent_id = parent?.let(::TransactionId),
        schedule = schedule?.let(::ScheduleId),
        reconciled = null,
        raw_synced_data = null,
      ),
    )
  }
}

// Walks down the page from the balance after its first row
private fun pageOf(rows: List<TransactionRow>, topBalance: Long): TransactionPage {
  var balance = topBalance
  return TransactionPage(rows, rows.map { row -> balance.also { balance -= row.amount } })
}

private fun Boolean.toLong() = if (this) 1L else 0L

@Suppress("LongParameterList")
private fun transactionRow(
  id: TransactionId,
  date: LocalDate,
  accountName: String?,
  payeeName: String?,
  transferAccountName: String?,
  notes: String?,
  categoryName: String?,
  amount: Long,
  isParent: Boolean?,
  isChild: Boolean?,
  error: JsonObject?,
  needsCategory: Long,
  offBudget: Long,
  isTransfer: Long,
  scheduleRecurring: Long?,
) =
  TransactionRow(
    id = id,
    date = date,
    accountName = accountName,
    payeeName = payeeName,
    transferAccountName = transferAccountName,
    notes = notes,
    categoryName = categoryName,
    amount = amount,
    isParent = isParent == true,
    isChild = isChild,
    needsCategory = needsCategory != 0L,
    offBudget = offBudget != 0L,
    isTransfer = isTransfer != 0L,
    scheduleRecurring = scheduleRecurring?.let { it != 0L },
    splitDifference = error?.takeIf { isParent == true }?.splitDifference(),
  )

// The difference of upstream's SplitTransactionError, in
// packages/loot-core/src/shared/transactions.ts
private fun JsonObject.splitDifference(): Long? {
  val type = (get("type") as? JsonPrimitive)?.contentOrNull
  if (type != "SplitTransactionError") return null
  return (get("difference") as? JsonPrimitive)?.longOrNull?.takeIf { it != 0L }
}

@Suppress("LongParameterList")
private fun childRow(
  id: TransactionId,
  date: LocalDate,
  accountName: String?,
  payeeName: String?,
  transferAccountName: String?,
  notes: String?,
  categoryName: String?,
  amount: Long,
  isParent: Boolean?,
  isChild: Boolean?,
  error: JsonObject?,
  needsCategory: Long,
  offBudget: Long,
  isTransfer: Long,
  scheduleRecurring: Long?,
  parentId: TransactionId?,
): Pair<TransactionId, TransactionRow> {
  val row =
    transactionRow(
      id,
      date,
      accountName,
      payeeName,
      transferAccountName,
      notes,
      categoryName,
      amount,
      isParent,
      isChild,
      error,
      needsCategory,
      offBudget,
      isTransfer,
      scheduleRecurring,
    )
  return (parentId ?: error("Child $id has no parent")) to row
}

@Suppress("LongParameterList")
private fun transactionDetail(
  id: TransactionId,
  date: LocalDate,
  accountName: String?,
  payeeName: String?,
  transferAccountName: String?,
  notes: String?,
  categoryName: String?,
  amount: Long,
  isParent: Boolean?,
  isChild: Boolean?,
  error: JsonObject?,
  needsCategory: Long,
  offBudget: Long,
  isTransfer: Long,
  scheduleRecurring: Long?,
  cleared: Boolean?,
  reconciled: Boolean?,
  parentId: TransactionId?,
  accountId: AccountId?,
  payeeId: PayeeId?,
  categoryId: CategoryId?,
) =
  TransactionDetail(
    row =
      transactionRow(
        id,
        date,
        accountName,
        payeeName,
        transferAccountName,
        notes,
        categoryName,
        amount,
        isParent,
        isChild,
        error,
        needsCategory,
        offBudget,
        isTransfer,
        scheduleRecurring,
      ),
    // Upstream's column defaults
    cleared = cleared != false,
    reconciled = reconciled == true,
    parent = parentId.takeIf { isChild == true },
    account = accountId,
    payee = payeeId,
    category = categoryId,
  )

// Null for closed accounts, which are grouped whichever budget they were in
private val AccountGroup.offBudget: Boolean?
  get() =
    when (this) {
      OnBudget -> false
      OffBudget -> true
      Closed -> null
    }

private fun transactionNotes(
  id: TransactionId,
  notes: String?,
  isChild: Boolean?,
  parentId: TransactionId?,
) = TransactionNotes(id, notes, parent = parentId.takeIf { isChild == true })

// Under SQLite's limit on bound parameters in one statement
private const val MAX_BIND_ARGS = 900
