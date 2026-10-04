package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.NotesContainingHash
import aktual.budget.db.Transactions
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
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

data class TransactionNotes(val id: TransactionId, val notes: String?)

data class TransactionRow(
  val id: TransactionId,
  val date: LocalDate,
  val accountName: String?,
  val payeeName: String?,
  val notes: String?,
  val categoryName: String?,
  val amount: Long,
  val isChild: Boolean?,
)

// One page of the list, with the balance after its first (newest) row
data class TransactionPage(val rows: List<TransactionRow>, val topBalance: Long)

@Inject
class TransactionDao(database: BudgetDatabase) {
  private val queries = database.transactionsQueries

  suspend fun getPaged(limit: Long, offset: Long): TransactionPage = queries.withResult {
    TransactionPage(
      rows = getPaged(limit, offset, ::TransactionRow).awaitAsList(),
      topBalance = balanceFromOffset(offset).awaitAsOne(),
    )
  }

  suspend fun getByAccountPaged(
    account: AccountId,
    limit: Long,
    offset: Long,
  ): TransactionPage = queries.withResult {
    TransactionPage(
      rows = getByAccountPaged(account, limit, offset, ::TransactionRow).awaitAsList(),
      topBalance = balanceFromOffsetByAccount(account, offset).awaitAsOne(),
    )
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

  // Rows come back in the order of the given IDs
  suspend fun getByIds(ids: List<TransactionId>): List<TransactionRow> = queries.withResult {
    val rows = getByIds(ids, ::TransactionRow).awaitAsList().associateBy { it.id }
    ids.mapNotNull(rows::get)
  }

  suspend fun getIdsAndNotes(): List<TransactionNotes> = queries.withResult {
    getIdsAndNotes().awaitAsList().map { TransactionNotes(it.id, it.notes) }
  }

  suspend fun getIdsAndNotesByAccount(account: AccountId): List<TransactionNotes> =
    queries.withResult {
      getIdsAndNotesByAccount(account).awaitAsList().map { TransactionNotes(it.id, it.notes) }
    }

  suspend fun getNotesContainingHash(): List<String> = queries.withResult {
    notesContainingHash().awaitAsList().mapNotNull(NotesContainingHash::notes)
  }

  suspend fun row(id: TransactionId): Transactions? = queries.withResult {
    getRowById(id).awaitAsOneOrNull()
  }

  // Every child of these split parents, deleted or not, as upstream's idsWithChildren()
  suspend fun childIds(parents: Collection<TransactionId>): List<TransactionId> =
    queries.withResult {
      parents.chunked(MAX_BIND_ARGS).flatMap { chunk -> getChildIds(chunk).awaitAsList() }
    }

  // Emits whenever a table behind the transactions view is written to
  fun observeChanges(): Flow<Unit> = queries.getIdsCount().asFlow().map {}

  suspend fun insert(
    id: String,
    account: String,
    category: String,
    payee: String,
    date: LocalDate,
    notes: String? = null,
    amount: Double = 0.0,
    isParent: Boolean = false,
    parent: String? = null,
  ) = queries.withoutResult {
    insert(
      Transactions(
        id = TransactionId(id),
        isParent = isParent,
        isChild = parent != null,
        acct = AccountId(account),
        category = CategoryId(category),
        amount = Amount(amount),
        description = PayeeId(payee),
        notes = notes,
        date = date,
        financial_id = null,
        type = null,
        location = null,
        error = null,
        imported_description = null,
        starting_balance_flag = null,
        transferred_id = null,
        sort_order = date.toEpochDays().days.inWholeMilliseconds.toDouble(),
        tombstone = false,
        cleared = null,
        pending = null,
        parent_id = parent?.let(::TransactionId),
        schedule = null,
        reconciled = null,
        raw_synced_data = null,
      )
    )
  }
}

// Under SQLite's limit on bound parameters in one statement
private const val MAX_BIND_ARGS = 900
