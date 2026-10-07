package aktual.budget.transactions.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.Timestamp
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestClock
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

internal val NOW = Instant.parse("2026-10-03T12:34:56.789Z")

internal data class Change(
  val dataset: String,
  val row: String,
  val column: String,
  val value: MessageValue,
)

// Changes sent to the controller are applied to the database, as BudgetSyncControllerImpl does
internal class WriterTestScope(
  val database: BudgetDatabase,
  val syncDao: SyncDao,
  val accountDao: AccountDao,
  val categoryDao: CategoryDao,
  val payeeDao: PayeeDao,
  val transactionDao: TransactionDao,
) : BudgetSyncController {
  val syncCalls = mutableListOf<List<LocalChange>>()

  private var nextId = 1

  val writer =
    TransactionWriter(
      syncController = this,
      accountDao = accountDao,
      categoryDao = categoryDao,
      payeeDao = payeeDao,
      transactionDao = transactionDao,
      uuidGenerator = { "id-${nextId++}" },
      clock = TestClock(NOW),
    )

  override suspend fun syncChanges(changes: List<LocalChange>) {
    syncCalls.add(changes)
    syncDao.sendMessages(changes)
  }

  override fun schedule() = Unit

  // What the last syncChanges() call sent, after checking it all reached messages_crdt
  suspend fun lastSync(): List<Change> {
    val sent = syncCalls.last().map { c -> Change(c.dataset, c.row, c.column, c.value) }
    val recorded = messages()
    val missing = sent.filterNot { it in recorded }
    check(missing.isEmpty()) { "Not recorded in messages_crdt: $missing" }
    return sent
  }

  // Everything recorded in messages_crdt
  suspend fun messages(): List<Change> =
    syncDao.getMessagesSince(Timestamp.fromMilliseconds(millis = 0)).map { m ->
      Change(m.dataset, m.row, m.column, m.value)
    }

  suspend fun insertAccount(id: AccountId, offBudget: Boolean = false) =
    accountDao.insert(id = id, name = id.value, offBudget = offBudget)

  // With its category_mapping row, which the transaction views resolve categories through
  suspend fun insertCategory(id: CategoryId, name: String, isIncome: Boolean = false) {
    database.categoryMappingQueries.insert(id, id)
    database.categoriesQueries.insert(
      Categories(
        id = id,
        name = name,
        is_income = isIncome,
        cat_group = null,
        sort_order = null,
        tombstone = false,
        hidden = false,
        goal_def = null,
        template_settings = null,
        cleanup_def = null,
      ),
    )
  }
}

internal fun runWriterTest(action: suspend WriterTestScope.() -> Unit) = runTest {
  val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
  driver.use {
    val database = buildDatabase(driver)
    val scope =
      WriterTestScope(
        database = database,
        syncDao = SyncDao(database, driver, Clock.System),
        accountDao = AccountDao(database),
        categoryDao = CategoryDao(database),
        payeeDao = PayeeDao(database),
        transactionDao = TransactionDao(database),
      )
    scope.action()
  }
}

internal fun string(value: String) = MessageValue.String(value)

internal fun number(value: Long) = MessageValue.Number(value)
