package aktual.budget.banksync.vm

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.BankId
import aktual.budget.model.BudgetId
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.inMemoryDriverFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BankSyncViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Groups open accounts by provider, with unlinked accounts separate`() = runBankSyncTest {
    insertBank(BANK_A, name = "Bankity Bank")
    insertAccount("checking", "Checking", SimpleFin, BANK_A, lastSync = NOW - 5.minutes)
    insertAccount("savings", "Savings", GoCardless, BANK_A, status = "reauth-required")
    insertAccount("credit", "Credit", SimpleFin, bank = null, status = "rate-limit-exceeded")
    insertAccount("cash", "Cash")
    insertAccount("closed", "Closed", GoCardless, BANK_A, closed = true)
    insertAccount("deleted", "Deleted", tombstone = true)
    val api = FakeBankSyncApi(SimpleFin to configured(), GoCardless to configured())

    val viewModel = createViewModel(api)

    viewModel.state.test {
      assertThat(awaitSettledSuccess())
        .isEqualTo(
          Success(
            providers =
              persistentListOf(
                BankSyncProvider(
                  source = GoCardless,
                  status = BankSyncProviderStatus.Configured,
                  accounts =
                    persistentListOf(
                      account(
                        "savings",
                        "Savings",
                        bank = "Bankity Bank",
                        status = BankSyncAccountStatus.ReauthRequired,
                      )
                    ),
                ),
                BankSyncProvider(
                  source = SimpleFin,
                  status = BankSyncProviderStatus.Configured,
                  accounts =
                    persistentListOf(
                      account(
                        "checking",
                        "Checking",
                        bank = "Bankity Bank",
                        lastSync = LastBankSync.MinutesAgo(minutes = 5),
                      ),
                      account("credit", "Credit", status = BankSyncAccountStatus.RateLimited),
                    ),
                ),
              ),
            unlinked = persistentListOf(account("cash", "Cash")),
          )
        )
    }
  }

  @Test
  fun `Shows each provider's status, or a failure`() = runBankSyncTest {
    insertAccount("a", "A", GoCardless)
    insertAccount("b", "B", SimpleFin)
    insertAccount("c", "C", PluggyAi)
    insertAccount("d", "D", AccountSyncSource.Akahu)
    val api =
      FakeBankSyncApi(
        GoCardless to configured(),
        SimpleFin to BankSyncStatusResponse.Success(configured = false),
        PluggyAi to BankSyncStatusResponse.Rejected(reason = "unauthorized", details = null),
        // Akahu isn't in the map, so it throws
      )

    val viewModel = createViewModel(api)

    viewModel.state.test {
      assertThat(awaitSettledSuccess().providers.associate { it.source to it.status })
        .isEqualTo(
          mapOf(
            AccountSyncSource.Akahu to BankSyncProviderStatus.Failed,
            GoCardless to BankSyncProviderStatus.Configured,
            PluggyAi to BankSyncProviderStatus.Failed,
            SimpleFin to BankSyncProviderStatus.NotConfigured,
          )
        )
    }
  }

  @Test
  fun `Local-only budget doesn't ask the server`() = runBankSyncTest {
    insertAccount("a", "A", GoCardless)
    val api = FakeBankSyncApi()

    val viewModel = createViewModel(api, server = BudgetServer.None)

    viewModel.state.test {
      assertThat(awaitSuccess().providers.single().status)
        .isEqualTo(BankSyncProviderStatus.NoServer)
    }
    assertThat(api.requested).isEmpty()
  }

  @Test
  fun `Only asks for providers that have accounts`() = runBankSyncTest {
    insertAccount("a", "A", SimpleFin)
    insertAccount("b", "B")
    val api = FakeBankSyncApi(SimpleFin to configured())

    val viewModel = createViewModel(api)

    viewModel.state.test { awaitSettledSuccess() }
    assertThat(api.requested).containsExactly(SimpleFin)
  }

  @Test
  fun `No open accounts is empty`() = runBankSyncTest {
    insertAccount("closed", "Closed", GoCardless, closed = true)

    val viewModel = createViewModel(FakeBankSyncApi())

    viewModel.state.test {
      var state = awaitItem()
      while (state == Loading) state = awaitItem()
      assertThat(state).isEqualTo(Empty)
    }
  }

  @Test
  fun `Last sync is relative to now`() {
    fun ago(duration: Duration) = lastBankSync(NOW - duration, NOW)
    assertThat(lastBankSync(null, NOW)).isEqualTo(LastBankSync.Never)
    assertThat(lastBankSync(NOW + 5.minutes, NOW)).isEqualTo(LastBankSync.JustNow)
    assertThat(ago(30.seconds)).isEqualTo(LastBankSync.JustNow)
    assertThat(ago(59.minutes)).isEqualTo(LastBankSync.MinutesAgo(minutes = 59))
    assertThat(ago(3.hours)).isEqualTo(LastBankSync.HoursAgo(hours = 3))
    assertThat(ago(40.days)).isEqualTo(LastBankSync.DaysAgo(days = 40))
  }

  private data class TestContext(
    val scope: TestScope,
    val database: BudgetDatabase,
    val driver: SqlDriver,
  )

  private fun runBankSyncTest(action: suspend TestContext.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use { action(TestContext(this, buildDatabase(it), it)) }
  }

  private fun TestContext.createViewModel(
    api: BankSyncApi,
    server: BudgetServer = REMOTE,
  ): BankSyncViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    return BankSyncViewModel(
      accountDao = AccountDao(database),
      api = api,
      server = server,
      clock = FixedClock,
    )
  }

  private suspend fun TestContext.insertBank(id: Uuid, name: String) {
    database.banksQueries.insert(id = id, bank_id = BankId(id.toString()), name = name)
  }

  @Suppress("LongParameterList")
  private suspend fun TestContext.insertAccount(
    id: String,
    name: String,
    source: AccountSyncSource? = null,
    bank: Uuid? = null,
    lastSync: Instant? = null,
    status: String? = null,
    closed: Boolean = false,
    tombstone: Boolean = false,
  ) {
    database.accountsQueries.insert(
      id = AccountId(id),
      account_id = source?.let { "$id-remote" },
      name = name,
      official_name = null,
      bank = bank,
      offbudget = false,
      account_sync_source = source,
    )

    // The insert query doesn't cover these, and nothing else in the app writes them yet
    val lastSyncValue = lastSync?.let { "'${it.toEpochMilliseconds()}'" } ?: "NULL"
    val statusValue = status?.let { "'$it'" } ?: "NULL"
    driver
      .execute(
        identifier = null,
        sql =
          "UPDATE accounts SET last_sync = $lastSyncValue, bank_sync_status = $statusValue, " +
            "closed = ${closed.toInt()}, tombstone = ${tombstone.toInt()} WHERE id = '$id'",
        parameters = 0,
      )
      .await()
  }

  private fun Boolean.toInt() = if (this) 1 else 0

  private fun account(
    id: String,
    name: String,
    bank: String? = null,
    lastSync: LastBankSync = LastBankSync.Never,
    status: BankSyncAccountStatus? = null,
  ) = BankSyncAccount(AccountId(id), name, bank, lastSync, status)

  private fun configured() = BankSyncStatusResponse.Success(configured = true)

  private suspend fun ReceiveTurbine<BankSyncState>.awaitSuccess(): Success {
    var state = awaitItem()
    while (state !is Success) state = awaitItem()
    cancelAndIgnoreRemainingEvents()
    return state
  }

  // Waits until every provider's status has come back
  private suspend fun ReceiveTurbine<BankSyncState>.awaitSettledSuccess(): Success {
    var state = awaitItem()
    while (state !is Success || state.providers.any { it.status == Checking }) {
      state = awaitItem()
    }
    cancelAndIgnoreRemainingEvents()
    return state
  }

  private class FakeBankSyncApi(vararg statuses: Pair<AccountSyncSource, BankSyncStatusResponse>) :
    BankSyncApi {
    private val statuses = statuses.toMap()
    val requested = mutableListOf<AccountSyncSource>()

    override suspend fun status(source: AccountSyncSource): BankSyncStatusResponse {
      requested += source
      return statuses[source] ?: error("No status for $source")
    }

    override suspend fun transactions(
      source: AccountSyncSource,
      request: BankSyncTransactionsRequest,
    ): BankSyncTransactionsResponse = error("Not used")
  }

  private object FixedClock : Clock {
    override fun now(): Instant = NOW
  }

  private companion object {
    val NOW = Instant.parse("2026-10-03T12:00:00Z")
    val BANK_A: Uuid = Uuid.parse("9707afb0-dd6a-4623-aab2-922f8e4ab38d")
    val REMOTE = BudgetServer.Remote(ServerUrl("https://actual.example.com"), Token("token"))
    val SimpleFin = AccountSyncSource.SimpleFin
    val GoCardless = AccountSyncSource.GoCardless
    val PluggyAi = AccountSyncSource.PluggyAi
  }
}
