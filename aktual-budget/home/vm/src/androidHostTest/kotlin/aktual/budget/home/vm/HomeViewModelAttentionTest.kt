package aktual.budget.home.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.home.vm.AttentionItem.OverdueSchedules
import aktual.budget.home.vm.AttentionItem.Overspent
import aktual.budget.home.vm.AttentionItem.SyncFailed
import aktual.budget.home.vm.AttentionItem.SyncFailedMany
import aktual.budget.home.vm.AttentionItem.Uncategorised
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.CategoryId
import aktual.test.TestCalendar
import aktual.test.inMemoryDriverFactory
import aktual.test.insertSchedule
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelAttentionTest {
  private val calendar = TestCalendar(LocalDate(2026, 4, 15))

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Nothing to act on is empty`() = runAttentionTest { viewModel, _, _ ->
    viewModel.state.test {
      assertThat(awaitAttention()).isEqualTo(Empty)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Items are in priority order`() =
    runAttentionTest(
      failingAccount("card", "reauth-required"),
      UNCATEGORISED,
      OVERSPENT_SPENDING,
      OVERSPENT_BUDGET,
      before = {
        insertSchedule(id = "s", name = "Missed", payee = "p", account = "a", nextDate = MISSED)
      },
    ) { viewModel, _, _ ->
      viewModel.state.test {
        assertThat(awaitItems())
          .containsExactly(
            SyncFailed(AccountId("card"), "card", ReauthRequired),
            Uncategorised(count = 1),
            Overspent(
              persistentListOf(OverspentCategory(CategoryId("food"), "Food", Amount(-20_000L))),
            ),
            OverdueSchedules(count = 1),
          )
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `Items come and go with their source`() = runAttentionTest { viewModel, _, driver ->
    viewModel.state.test {
      assertThat(awaitAttention()).isEqualTo(Empty)

      driver.run(UNCATEGORISED)
      driver.notifyListeners("transactions")
      assertThat(awaitItems()).containsExactly(Uncategorised(count = 1))

      driver.run(failingAccount("card", "failed"))
      driver.notifyListeners("accounts")
      assertThat(awaitItems())
        .containsExactly(
          SyncFailed(AccountId("card"), "card", Failed),
          Uncategorised(count = 1),
        )

      driver.run("UPDATE transactions SET tombstone = 1 WHERE id = 'u1'")
      driver.notifyListeners("transactions")
      assertThat(awaitItems()).containsExactly(SyncFailed(AccountId("card"), "card", Failed))

      driver.run("UPDATE accounts SET bank_sync_status = 'ok' WHERE id = 'card'")
      driver.notifyListeners("accounts")
      assertThat(awaitAttention()).isEqualTo(Empty)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Up to three failing accounts get a row each`() =
    runAttentionTest(
      failingAccount("a", "failed"),
      failingAccount("b", "timed-out"),
      failingAccount("c", "account-missing"),
    ) { viewModel, _, _ ->
      viewModel.state.test {
        assertThat(awaitItems())
          .containsExactly(
            SyncFailed(AccountId("a"), "a", Failed),
            SyncFailed(AccountId("b"), "b", TimedOut),
            SyncFailed(AccountId("c"), "c", AccountMissing),
          )
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `More failing accounts collapse into one row`() =
    runAttentionTest(
      failingAccount("a", "failed"),
      failingAccount("b", "failed"),
      failingAccount("c", "failed"),
      failingAccount("d", "failed"),
    ) { viewModel, _, _ ->
      viewModel.state.test {
        assertThat(awaitItems()).containsExactly(SyncFailedMany(count = 4))
        cancelAndIgnoreRemainingEvents()
      }
    }

  private suspend fun ReceiveTurbine<HomeState>.awaitAttention() = awaitSettled().attention

  private suspend fun ReceiveTurbine<HomeState>.awaitItems() = (awaitAttention() as Loaded).items

  private fun runAttentionTest(
    vararg setup: String,
    before: suspend BudgetDatabase.() -> Unit = {},
    action: suspend TestScope.(HomeViewModel, BudgetDatabase, SqlDriver) -> Unit,
  ) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    driver.use {
      val database = buildDatabase(driver)
      (SETUP + setup).forEach { sql -> driver.run(sql) }
      database.before()
      action(database.createHomeViewModel(this, calendar), database, driver)
    }
  }

  private suspend fun SqlDriver.run(sql: String) {
    execute(identifier = null, sql = sql, parameters = 0).await()
  }

  private companion object {
    val MISSED = LocalDate(2026, 4, 1)

    @Suppress("MaxLineLength")
    val SETUP =
      listOf(
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('usual', 'Usual', 0, 1)",
        "INSERT INTO category_groups(id, name, is_income, sort_order) VALUES ('income', 'Income', 1, 2)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('food', 'Food', 0, 'usual', 1)",
        "INSERT INTO categories(id, name, is_income, cat_group, sort_order) VALUES ('salary', 'Salary', 1, 'income', 1)",
        "INSERT INTO category_mapping(id, transferId) VALUES ('food', 'food'), ('salary', 'salary')",
        "INSERT INTO accounts(id, name, offbudget) VALUES ('on', 'On', 0)",
      )

    const val UNCATEGORISED =
      "INSERT INTO transactions(id, acct, amount, date) VALUES ('u1', 'on', -1000, 20260403)"

    // Food ends up 200.00 over
    const val OVERSPENT_SPENDING =
      "INSERT INTO transactions(id, acct, category, amount, date) VALUES ('t1', 'on', 'food', -50000, 20260402)"

    const val OVERSPENT_BUDGET =
      "INSERT INTO zero_budgets(id, month, category, amount) VALUES ('202604-food', 202604, 'food', 30000)"

    fun failingAccount(id: String, status: String) =
      "INSERT INTO accounts(id, name, offbudget, account_id, account_sync_source, bank_sync_status) " +
        "VALUES ('$id', '$id', 0, 'remote-$id', 'simpleFin', '$status')"
  }
}
