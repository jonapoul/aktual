package aktual.budget.banksync.vm.link

import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.Rejected
import aktual.api.model.banksync.ExternalBankAccount
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBank
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
import aktual.budget.BudgetSyncController
import aktual.budget.banksync.domain.BankAccountLinker
import aktual.budget.banksync.domain.GoCardlessLoginWaiter
import aktual.budget.banksync.vm.FakeBankSyncApi
import aktual.budget.banksync.vm.FakeBankSyncController
import aktual.budget.banksync.vm.link.ExternalAccounts.NeedsLogin
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.BankId
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.di.BudgetCoroutineScope
import aktual.test.TestBuildConfig
import aktual.test.inMemoryDriverFactory
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlin.test.AfterTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
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
class LinkBankAccountViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Lists the first configured provider's accounts`() = runLinkTest {
    insertLinked(AccountId("other"), "Other", remote = "ACT-2")
    api.accounts[PluggyAi] = BankSyncAccountsResponse.Success(listOf(CHECKING, CARD))

    createViewModel().state.test {
      assertThat(awaitChoosing())
        .isEqualTo(
          LinkBankAccountState.Choosing(
            accountName = "Cash",
            providers = persistentListOf(PluggyAi, Akahu),
            selected = PluggyAi,
            accounts =
              ExternalAccounts.Loaded(
                persistentListOf(
                  ExternalAccountItem("ACT-1", "Checking", "My Bank", Amount(12.34)),
                  ExternalAccountItem("ACT-2", "Card", null, null, linkedTo = "Other"),
                )
              ),
          )
        )
    }
    assertThat(api.listed).containsExactly(PluggyAi)
  }

  @Test
  fun `No server means no providers`() = runLinkTest {
    createViewModel(server = None).state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(NoProviders)
    }
    assertThat(api.requested).isEmpty()
  }

  @Test
  fun `Missing account fails`() = runLinkTest {
    createViewModel(AccountId("missing")).state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(LinkBankAccountState.Failure(cause = null))
    }
  }

  @Test
  fun `Failed listing can be retried`() = runLinkTest {
    api.accounts[PluggyAi] = BankSyncAccountsResponse.Failed(Rejected("Bad token", null))
    val viewModel = createViewModel()

    viewModel.state.test {
      assertThat(awaitChoosing().accounts).isEqualTo(ExternalAccounts.Failure("Bad token"))

      api.accounts[PluggyAi] = BankSyncAccountsResponse.Success(listOf(CHECKING))
      viewModel.reload()
      assertThat(awaitChoosing().accounts)
        .isEqualTo(
          ExternalAccounts.Loaded(
            persistentListOf(ExternalAccountItem("ACT-1", "Checking", "My Bank", Amount(12.34)))
          )
        )
    }
  }

  @Test
  fun `Selecting another provider lists its accounts`() = runLinkTest {
    api.accounts[PluggyAi] = BankSyncAccountsResponse.Success(listOf(CHECKING))
    api.accounts[Akahu] = BankSyncAccountsResponse.Success(listOf(CARD))
    val viewModel = createViewModel()

    viewModel.state.test {
      awaitChoosing()
      viewModel.select(Akahu)
      val choosing = awaitChoosing()
      assertThat(choosing.selected).isEqualTo(Akahu)
      assertThat(choosing.accounts)
        .isEqualTo(
          ExternalAccounts.Loaded(
            persistentListOf(ExternalAccountItem("ACT-2", "Card", null, null))
          )
        )
    }
    assertThat(api.listed).containsExactly(PluggyAi, Akahu)
  }

  @Test
  fun `Linking stores the link, syncs and announces it`() = runLinkTest {
    api.accounts[PluggyAi] = BankSyncAccountsResponse.Success(listOf(CHECKING))
    val viewModel = createViewModel()

    viewModel.state.test {
      awaitChoosing()
      viewModel.events.test {
        viewModel.link("ACT-1")
        assertThat(awaitItem()).isEqualTo(LinkBankAccountEvent.Linked)
      }
      cancelAndIgnoreRemainingEvents()
    }
    scope.testScheduler.runCurrent()

    val row = AccountDao(database)[ACCOUNT]
    assertThat(row?.account_id).isEqualTo("ACT-1")
    assertThat(row?.account_sync_source).isEqualTo(PluggyAi)
    assertThat(row?.bank).isNotNull()
    assertThat(controller.synced).containsExactly(setOf(ACCOUNT))
  }

  @Test
  fun `GoCardless lists the banks to log in to`() =
    runLinkTest(goCardless = true) {
      api.banks["GB"] = GoCardlessBanksResponse.Success(listOf(MONZO))
      api.banks["IE"] = GoCardlessBanksResponse.Failed(Rejected("Bad secret", null))
      val viewModel = createViewModel()

      viewModel.state.test {
        val login = awaitLogin()
        assertThat(login)
          .isEqualTo(
            GoCardlessLogin(
              countries = login.countries,
              country = "GB",
              banks =
                GoCardlessBanks.Loaded(persistentListOf(GoCardlessBankItem(MONZO.id, "Monzo"))),
            )
          )

        viewModel.selectCountry("IE")
        assertThat(awaitLogin().banks).isEqualTo(GoCardlessBanks.Failure("Bad secret"))
        cancelAndIgnoreRemainingEvents()
      }
      assertThat(api.bankRequests).containsExactly("GB", "IE")
    }

  @Test
  fun `Logging in through GoCardless lists the shared accounts to link`() =
    runLinkTest(goCardless = true) {
      api.banks["GB"] = GoCardlessBanksResponse.Success(listOf(MONZO))
      api.login = GoCardlessLoginResponse.Success(LINK, REQUISITION)
      api.polls += GoCardlessAccountsResponse.Pending
      api.polls += GoCardlessAccountsResponse.Success(listOf(SHARED))
      val viewModel = createViewModel()

      viewModel.state.test {
        awaitLogin()
        viewModel.events.test {
          viewModel.logIn(MONZO.id)
          assertThat(awaitItem()).isEqualTo(LinkBankAccountEvent.OpenBrowser(LINK))

          var accounts = awaitChoosing().accounts
          while (accounts is NeedsLogin) accounts = awaitChoosing().accounts
          assertThat(accounts)
            .isEqualTo(
              ExternalAccounts.Loaded(
                persistentListOf(ExternalAccountItem("GC-1", "Current", "Monzo", null))
              )
            )
          viewModel.link("GC-1")
          assertThat(awaitItem()).isEqualTo(LinkBankAccountEvent.Linked)
        }
        cancelAndIgnoreRemainingEvents()
      }
      scope.testScheduler.runCurrent()

      val row = AccountDao(database)[ACCOUNT]
      assertThat(row?.account_id).isEqualTo("GC-1")
      assertThat(row?.account_sync_source).isEqualTo(GoCardless)
      assertThat(BankSyncDao(database).bankId(checkNotNull(row?.bank)))
        .isEqualTo(BankId(REQUISITION))
      assertThat(api.logins).containsExactly(MONZO.id)
    }

  @Test
  fun `GoCardless logins time out`() =
    runLinkTest(goCardless = true) {
      api.banks["GB"] = GoCardlessBanksResponse.Success(listOf(MONZO))
      api.login = GoCardlessLoginResponse.Success(LINK, REQUISITION)
      val viewModel = createViewModel()

      viewModel.state.test {
        awaitLogin()
        viewModel.logIn(MONZO.id)
        var status = awaitLogin().status
        while (status !is Failed) status = awaitLogin().status
        assertThat(status).isEqualTo(GoCardlessLoginStatus.Failed(cause = null, isTimeout = true))
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Cancelling a GoCardless login stops waiting`() =
    runLinkTest(goCardless = true) {
      api.banks["GB"] = GoCardlessBanksResponse.Success(listOf(MONZO))
      api.login = GoCardlessLoginResponse.Success(LINK, REQUISITION)
      val viewModel = createViewModel()

      viewModel.state.test {
        awaitLogin()
        viewModel.logIn(MONZO.id)
        var status = awaitLogin().status
        while (status != GoCardlessLoginStatus.Waiting("Monzo", LINK)) status = awaitLogin().status

        viewModel.cancelLogin()
        assertThat(awaitLogin().status).isEqualTo(Idle)
        scope.testScheduler.advanceTimeBy(1.minutes)
        expectNoEvents()
      }
    }

  private class TestContext(
    val scope: TestScope,
    val database: BudgetDatabase,
    driver: SqlDriver,
    goCardless: Boolean,
  ) : BudgetSyncController {
    val api =
      FakeBankSyncApi(
        AccountSyncSource.SimpleFin to BankSyncStatusResponse.Success(configured = false),
        PluggyAi to BankSyncStatusResponse.Success(configured = true),
        Akahu to BankSyncStatusResponse.Success(configured = true),
        GoCardless to BankSyncStatusResponse.Success(configured = goCardless),
      )
    val controller = FakeBankSyncController()
    private val syncDao = SyncDao(database, driver, Clock.System)

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit
  }

  // GoCardless comes first when it's configured, so tests of the other providers leave it out
  private fun runLinkTest(goCardless: Boolean = false, action: suspend TestContext.() -> Unit) =
    runTest {
      val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
      driver.use { d ->
        val context = TestContext(this, buildDatabase(d), d, goCardless)
        AccountDao(context.database).insert(id = ACCOUNT, name = "Cash")
        action(context)
      }
    }

  private fun TestContext.createViewModel(
    account: AccountId = ACCOUNT,
    server: BudgetServer = REMOTE,
  ): LinkBankAccountViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    return LinkBankAccountViewModel(
      account = account,
      accountDao = AccountDao(database),
      api = api,
      linker =
        BankAccountLinker(
          api = api,
          accountDao = AccountDao(database),
          dao = BankSyncDao(database),
          syncController = this,
          bankSync = controller,
          uuidGenerator = { Uuid.random().toString() },
          scope = BudgetCoroutineScope(scope.backgroundScope),
        ),
      server = server,
      waiter = GoCardlessLoginWaiter(api),
      buildConfig = TestBuildConfig,
    )
  }

  private suspend fun TestContext.insertLinked(id: AccountId, name: String, remote: String) {
    database.accountsQueries.insert(
      id = id,
      account_id = remote,
      name = name,
      official_name = null,
      bank = null,
      offbudget = false,
      account_sync_source = PluggyAi,
    )
  }

  private suspend fun ReceiveTurbine<LinkBankAccountState>.awaitChoosing():
    LinkBankAccountState.Choosing {
    var item = awaitItem()
    while (item !is Choosing || item.accounts == Loading) {
      item = awaitItem()
    }
    return item
  }

  // Skips past the banks loading
  private suspend fun ReceiveTurbine<LinkBankAccountState>.awaitLogin(): GoCardlessLogin {
    while (true) {
      val login = ((awaitItem() as? LinkBankAccountState.Choosing)?.accounts as? NeedsLogin)?.login
      if (login != null && login.banks != Loading) return login
    }
  }

  private companion object {
    val ACCOUNT = AccountId("account-1")
    val REMOTE = BudgetServer.Remote(ServerUrl("https://actual.example.com"), Token("token"))
    val PluggyAi = AccountSyncSource.PluggyAi
    val Akahu = AccountSyncSource.Akahu
    val GoCardless = AccountSyncSource.GoCardless
    val MONZO = GoCardlessBank("MONZO_MONZGB2L", "Monzo")
    const val LINK = "https://ob.gocardless.com/start/requisition-1"
    const val REQUISITION = "requisition-1"
    val SHARED = ExternalBankAccount("GC-1", "Current", "Monzo", REQUISITION, null, null)
    val CHECKING = ExternalBankAccount("ACT-1", "Checking", "My Bank", "ORG-1", null, Amount(12.34))
    val CARD = ExternalBankAccount("ACT-2", "Card", null, null, null, null)
  }
}
