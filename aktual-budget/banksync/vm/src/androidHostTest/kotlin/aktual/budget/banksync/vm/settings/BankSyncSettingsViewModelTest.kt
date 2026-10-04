package aktual.budget.banksync.vm.settings

import aktual.budget.BudgetSyncController
import aktual.budget.banksync.domain.BankAccountLinker
import aktual.budget.banksync.domain.BankSyncSettingsLoader
import aktual.budget.banksync.domain.BankSyncSettingsWriter
import aktual.budget.banksync.domain.MappableFieldsLoader
import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.vm.FakeBankSyncApi
import aktual.budget.banksync.vm.FakeBankSyncController
import aktual.budget.banksync.vm.settings.BankSyncSettingsState.Editing
import aktual.budget.banksync.vm.settings.BankSyncSettingsState.Failure
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.AccountId
import aktual.budget.model.BudgetId
import aktual.budget.model.LocalChange
import aktual.budget.model.SyncedPrefKey.PerAccount.CustomSyncMappings
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportNotes
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncImportTransactions
import aktual.budget.model.SyncedPrefKey.PerAccount.SyncUpdateDates
import aktual.di.BudgetCoroutineScope
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.time.Clock
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
class BankSyncSettingsViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Unset settings take their defaults`() = runSettingsTest {
    createViewModel().state.test {
      assertThat(awaitEditing())
        .isEqualTo(
          Editing(
            accountName = "Checking",
            importTransactions = true,
            importPending = true,
            importNotes = true,
            reimportDeleted = true,
            updateDates = false,
            direction = Payment,
            fields = null,
            hasChanges = false,
          )
        )
    }
  }

  @Test
  fun `Saved settings are loaded`() = runSettingsTest {
    preferences[SyncImportNotes(ACCOUNT)] = "false"
    preferences[SyncImportTransactions(ACCOUNT)] = "false"

    createViewModel().state.test {
      val editing = awaitEditing()
      assertThat(editing.importNotes).isFalse()
      assertThat(editing.importTransactions).isFalse()
    }
  }

  @Test
  fun `Missing account fails`() = runSettingsTest {
    createViewModel(AccountId("missing")).state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(Failure(cause = null))
    }
  }

  @Test
  fun `Invalid saved mappings fail`() = runSettingsTest {
    preferences[CustomSyncMappings(ACCOUNT)] = "[]"

    createViewModel().state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isInstanceOf<Failure>()
    }
  }

  @Test
  fun `Fields come from each direction's newest synced transaction`() = runSettingsTest {
    insertSynced("t1", amount = -100, raw = """{"date":"2026-09-30","payeeName":"Tesco"}""")
    insertSynced("t2", amount = 200, raw = """{"valueDate":"2026-09-29","debtorName":"Work"}""")
    val viewModel = createViewModel()

    viewModel.state.test {
      assertThat(awaitEditing().fields)
        .isEqualTo(
          persistentListOf(
            row(Date, "date", FieldOption("date", "2026-09-30")),
            row(Payee, "payeeName", FieldOption("payeeName", "Tesco")),
            row(Notes, "notes"),
          )
        )

      viewModel.setDirection(Deposit)
      val deposit = awaitItem() as Editing
      assertThat(deposit.direction).isEqualTo(Deposit)
      assertThat(deposit.fields)
        .isEqualTo(
          persistentListOf(
            row(Date, "date", FieldOption("valueDate", "2026-09-29")),
            row(Payee, "payeeName", FieldOption("debtorName", "Work")),
            row(Notes, "notes"),
          )
        )

      viewModel.setMapping(Payee, "debtorName")
      val mapped = awaitItem() as Editing
      assertThat(mapped.fields?.get(1)?.example).isEqualTo("Work")
      assertThat(mapped.hasChanges).isTrue()
    }
  }

  @Test
  fun `Saving sends the settings and announces it`() = runSettingsTest {
    val viewModel = createViewModel()

    viewModel.state.test {
      awaitEditing()
      viewModel.set(UpdateDates, true)
      assertThat((awaitItem() as Editing).hasChanges).isTrue()

      viewModel.events.test {
        viewModel.save()
        assertThat(awaitItem()).isEqualTo(Saved)
      }
      assertThat((awaitItem() as Editing).hasChanges).isFalse()
    }

    assertThat(preferences[SyncUpdateDates(ACCOUNT)]).isEqualTo("true")
    assertThat(syncCalls.size).isEqualTo(1)
  }

  @Test
  fun `Toggles change their settings`() = runSettingsTest {
    val viewModel = createViewModel()

    viewModel.state.test {
      awaitEditing()
      viewModel.set(ImportTransactions, false)
      viewModel.set(ImportPending, false)
      viewModel.set(ImportNotes, false)
      viewModel.set(ReimportDeleted, false)
      viewModel.set(UpdateDates, true)

      var editing = awaitEditing()
      while (!editing.updateDates) editing = awaitEditing()
      assertThat(editing.importTransactions).isFalse()
      assertThat(editing.importPending).isFalse()
      assertThat(editing.importNotes).isFalse()
      assertThat(editing.reimportDeleted).isFalse()
      assertThat(editing.updateDates).isTrue()
      assertThat(syncCalls.firstOrNull()).isNull()
    }
  }

  @Test
  fun `Unlinking clears the link and announces it`() = runSettingsTest {
    database.accountsQueries.insert(
      id = LINKED,
      account_id = "remote-1",
      name = "Linked",
      official_name = null,
      bank = BANK,
      offbudget = false,
      account_sync_source = SimpleFin,
    )
    val viewModel = createViewModel(LINKED)

    viewModel.state.test {
      awaitEditing()
      viewModel.events.test {
        viewModel.unlink()
        assertThat(awaitItem()).isEqualTo(Unlinked)
      }
      cancelAndIgnoreRemainingEvents()
    }

    val row = AccountDao(database)[LINKED]
    assertThat(row?.account_id).isNull()
    assertThat(row?.bank).isNull()
    assertThat(row?.account_sync_source).isNull()
  }

  @Test
  fun `Failing to unlink announces it`() = runSettingsTest {
    val viewModel = createViewModel(AccountId("missing"))

    viewModel.events.test {
      viewModel.unlink()
      assertThat(awaitItem()).isInstanceOf<BankSyncSettingsEvent.UnlinkFailed>()
    }
  }

  private class TestContext(
    val scope: TestScope,
    val database: BudgetDatabase,
    val driver: SqlDriver,
  ) : BudgetSyncController {
    val syncCalls = mutableListOf<List<LocalChange>>()
    val api = FakeBankSyncApi()
    val preferences = PreferencesDao(database, TestCoroutineContexts(EmptyCoroutineContext))
    private val syncDao = SyncDao(database, driver, Clock.System)

    override suspend fun syncChanges(changes: List<LocalChange>) {
      syncCalls.add(changes)
      syncDao.sendMessages(changes)
    }

    override fun schedule() = Unit
  }

  private fun runSettingsTest(action: suspend TestContext.() -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use { d ->
      val context = TestContext(this, buildDatabase(d), d)
      AccountDao(context.database).insert(id = ACCOUNT, name = "Checking")
      action(context)
    }
  }

  private fun TestContext.createViewModel(account: AccountId = ACCOUNT): BankSyncSettingsViewModel {
    Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
    return BankSyncSettingsViewModel(
      account = account,
      accountDao = AccountDao(database),
      loader = BankSyncSettingsLoader(preferences),
      writer = BankSyncSettingsWriter(this),
      fieldsLoader = MappableFieldsLoader(BankSyncDao(database)),
      linker =
        BankAccountLinker(
          api = api,
          accountDao = AccountDao(database),
          dao = BankSyncDao(database),
          syncController = this,
          bankSync = FakeBankSyncController(),
          uuidGenerator = { Uuid.random().toString() },
          scope = BudgetCoroutineScope(scope.backgroundScope),
        ),
    )
  }

  private fun TestContext.insertSynced(id: String, amount: Long, raw: String) {
    driver.execute(
      identifier = null,
      sql =
        "INSERT INTO transactions (id, acct, amount, date, raw_synced_data) " +
          "VALUES ('$id', '${ACCOUNT.value}', $amount, 20260930, '$raw')",
      parameters = 0,
    )
  }

  private fun row(
    field: MappedField,
    selected: String,
    vararg options: FieldOption,
  ) = MappedFieldRow(field, selected, persistentListOf(*options))

  private suspend fun ReceiveTurbine<BankSyncSettingsState>.awaitEditing(): Editing {
    var item = awaitItem()
    while (item !is Editing) item = awaitItem()
    return item
  }

  private companion object {
    val ACCOUNT = AccountId("account-1")
    val LINKED = AccountId("account-2")
    val BANK: Uuid = Uuid.parse("9707afb0-dd6a-4623-aab2-922f8e4ab38d")
  }
}
