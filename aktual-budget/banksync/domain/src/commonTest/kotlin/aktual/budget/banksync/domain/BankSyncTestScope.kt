package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransaction
import aktual.budget.BudgetSyncController
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.Rules
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.BankId
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.Timestamp
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.di.BudgetCoroutineScope
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestClock
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal val NOW = Instant.parse("2026-10-03T12:34:56.789Z")
internal val TODAY = LocalDate(2026, 10, 3)
internal val ACCOUNT = AccountId("account-1")

internal data class Change(
  val dataset: String,
  val row: String,
  val column: String,
  val value: MessageValue,
)

// Changes sent to the controller are applied to the database, as BudgetSyncControllerImpl does
internal class BankSyncTestScope(
  val database: BudgetDatabase,
  private val driver: SqlDriver,
  private val syncDao: SyncDao,
  private val backgroundScope: CoroutineScope,
) : BudgetSyncController {
  val syncCalls = mutableListOf<List<LocalChange>>()
  val transactionDao = TransactionDao(database)
  val payeeDao = PayeeDao(database)
  val preferences = PreferencesDao(database, TestCoroutineContexts(EmptyCoroutineContext))

  val accountDao = AccountDao(database)
  val dao = BankSyncDao(database)
  private var nextId = 1

  val writer =
    TransactionWriter(
      syncController = this,
      accountDao = accountDao,
      categoryDao = CategoryDao(database),
      payeeDao = payeeDao,
      transactionDao = transactionDao,
      uuidGenerator = ::uuid,
      clock = TestClock(NOW),
    )

  val importer =
    BankSyncImporter(
      writer = writer,
      rulesLoader =
        TransactionRulesLoader(
          rulesDao = RulesDao(database),
          contextDao = RuleContextDao(database),
          uuidGenerator = ::uuid,
        ),
      settingsLoader = BankSyncSettingsLoader(preferences),
      dao = BankSyncDao(database),
      uuidGenerator = ::uuid,
      calendar = { TODAY },
      clock = TestClock(NOW),
    )

  private fun uuid() = "id-${nextId++}"

  // Its background work runs in the test's background scope
  fun controller(api: FakeBankSyncApi) =
    BankSyncControllerImpl(
      api = api,
      importer = importer,
      writer = writer,
      dao = BankSyncDao(database),
      calendar = { TODAY },
      clock = TestClock(NOW),
      scope = BudgetCoroutineScope(backgroundScope),
    )

  override suspend fun syncChanges(changes: List<LocalChange>) {
    syncCalls.add(changes)
    syncDao.sendMessages(changes)
  }

  override fun schedule() = Unit

  // Everything recorded in messages_crdt
  suspend fun messages(): List<Change> =
    syncDao.getMessagesSince(Timestamp.fromMilliseconds(millis = 0)).map { m ->
      Change(m.dataset, m.row, m.column, m.value)
    }

  // What the last syncChanges() call sent
  fun lastSync(): List<Change> =
    syncCalls.last().map { c -> Change(c.dataset, c.row, c.column, c.value) }

  suspend fun import(
    vararg transactions: JsonObject,
    source: AccountSyncSource? = null,
    initialSync: Boolean = false,
  ) =
    importer.import(
      ACCOUNT,
      source,
      BankSyncDownload(transactions.map(::BankSyncTransaction)),
      initialSync,
    )

  suspend fun insertAccount(id: AccountId = ACCOUNT, offBudget: Boolean = false) =
    accountDao.insert(id = id, name = id.value, offBudget = offBudget)

  // An account linked to a bank, whose provider calls it [accountId]
  @Suppress("LongParameterList")
  suspend fun insertLinkedAccount(
    id: AccountId,
    accountId: String = "provider-${id.value}",
    source: AccountSyncSource? = AccountSyncSource.GoCardless,
    bankId: String = "bank-${id.value}",
    bankName: String? = "Bank ${id.value}",
    offBudget: Boolean = false,
  ) {
    val bank = Uuid.random()
    database.banksQueries.insert(bank, BankId(bankId), bankName)
    accountDao.insert(
      id = id,
      accountId = accountId,
      name = id.value,
      bank = bank,
      offBudget = offBudget,
      accountSyncSource = source,
    )
  }

  suspend fun account(id: AccountId) = checkNotNull(accountDao[id]) { "No $id" }

  // With its category_mapping row, which the transaction views resolve categories through
  suspend fun insertCategory(id: CategoryId) {
    database.categoryMappingQueries.insert(id, id)
    database.categoriesQueries.insert(
      Categories(
        id = id,
        name = id.value,
        is_income = false,
        cat_group = null,
        sort_order = null,
        tombstone = false,
        hidden = false,
        goal_def = null,
        template_settings = null,
        cleanup_def = null,
      )
    )
  }

  suspend fun insertRule(
    id: String,
    conditions: List<Condition>,
    actions: List<RuleAction>,
    tombstone: Boolean = false,
  ) =
    RulesDao(database)
      .insert(
        Rules(
          id = RuleId(id),
          stage = null,
          conditions = conditions,
          actions = actions,
          tombstone = tombstone,
          conditions_op = ConditionOp.And,
        )
      )

  // Live transactions in the account, newest first, as v_transactions orders them
  fun liveIds(account: AccountId = ACCOUNT): List<TransactionId> =
    query(
        "SELECT id FROM v_transactions WHERE account = '${account.value}' " +
          "ORDER BY date DESC, starting_balance_flag, sort_order DESC, id"
      )
      .map(::TransactionId)

  fun payeeNames(): List<String> =
    query("SELECT name FROM payees WHERE tombstone = 0 AND name IS NOT NULL ORDER BY name")

  suspend fun row(id: TransactionId) = checkNotNull(transactionDao.row(id)) { "No $id" }

  // The JDBC driver closes the cursor once the mapper returns, so it's read synchronously
  private fun query(sql: String): List<String> =
    driver
      .executeQuery(
        identifier = null,
        sql = sql,
        parameters = 0,
        mapper = { cursor ->
          QueryResult.Value(
            buildList { while (cursor.next().value) add(checkNotNull(cursor.getString(0))) }
          )
        },
      )
      .value
}

internal fun runBankSyncTest(action: suspend BankSyncTestScope.() -> Unit) = runTest {
  val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
  driver.use {
    val database = buildDatabase(driver)
    val syncDao = SyncDao(database, driver, Clock.System)
    val scope = BankSyncTestScope(database, driver, syncDao, backgroundScope)
    scope.insertAccount()
    scope.action()
  }
}

/**
 * A downloaded transaction's JSON with the fields providers send, plus any [extra] ones. JSON
 * rather than BankSyncTransaction, since value classes can't be varargs.
 */
@Suppress("LongParameterList")
internal fun bankTx(
  amount: String,
  date: String = "2026-09-30",
  payeeName: String = "Tesco",
  transactionId: String? = null,
  internalTransactionId: String? = null,
  booked: Boolean = true,
  notes: String? = null,
  extra: JsonObjectBuilder.() -> Unit = {},
) = buildJsonObject {
  put("booked", booked)
  put("date", date)
  put("payeeName", payeeName)
  notes?.let { put("notes", it) }
  transactionId?.let { put("transactionId", it) }
  internalTransactionId?.let { put("internalTransactionId", it) }
  putJsonObject("transactionAmount") {
    put("amount", amount)
    put("currency", "GBP")
  }
  extra()
}

internal fun string(value: String) = MessageValue.String(value)
