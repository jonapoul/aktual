package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.NotesContainingHash
import aktual.budget.db.Transactions
import aktual.budget.db.transactions.GetById
import aktual.budget.db.withResult
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.LocalDate

data class TransactionNotes(val id: TransactionId, val notes: String?)

@Inject
class TransactionDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.transactionsQueries

  fun observeById(id: TransactionId): Flow<GetById?> =
    queries.getById(id).asFlow().mapToOneOrNull(contexts.default).distinctUntilChanged()

  suspend fun getIdsPaged(limit: Long, offset: Long): List<TransactionId> = queries.withResult {
    getIdsPaged(limit, offset).awaitAsList()
  }

  suspend fun getIdsByAccountPaged(
    account: AccountId,
    limit: Long,
    offset: Long,
  ): List<TransactionId> = queries.withResult {
    getIdsByAccountPaged(account, limit, offset).awaitAsList()
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

  fun observeCount(): Flow<Long> =
    queries.getIdsCount().asFlow().mapToOne(contexts.default).distinctUntilChanged()

  fun observeCountByAccount(account: AccountId): Flow<Long> =
    queries.getIdsByAccountCount(account).asFlow().mapToOne(contexts.default).distinctUntilChanged()

  suspend fun insert(
    id: String,
    account: String,
    category: String,
    payee: String,
    date: LocalDate,
    notes: String? = null,
    amount: Double = 0.0,
  ) = queries.withoutResult {
    insert(
      Transactions(
        id = TransactionId(id),
        isParent = false,
        isChild = false,
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
        tombstone = null,
        cleared = null,
        pending = null,
        parent_id = null,
        schedule = null,
        reconciled = null,
        raw_synced_data = null,
      )
    )
  }
}
