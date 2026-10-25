package aktual.budget.rules.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.BudgetId
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.RulesLoader
import aktual.budget.rules.vm.list.ListRulesState.Failure
import aktual.budget.rules.vm.list.ListRulesState.Loading
import aktual.budget.rules.vm.list.ListRulesState.Success
import aktual.test.TestSyncController
import aktual.test.inMemoryDriverFactory
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import app.cash.sqldelight.db.SqlDriver
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class ListRulesViewModelTest {
  @Test
  fun `Rules written to the database show up without a reload`() = runDatabaseTest { scope ->
    insertRule(id = "a")
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThat(awaitLoaded())
        .isInstanceOf(Success::class)
        .prop(Success::rules)
        .extracting { it.id.value }
        .containsExactly("a")

      insertRule(id = "b")

      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::rules)
        .extracting { it.id.value }
        .containsExactly("a", "b")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Retry recovers from a failed load`() = runTest {
    inMemoryDriverFactory().create(BudgetId("abc-123")).use { driver ->
      val database = buildDatabase(driver)
      driver.run("INSERT INTO rules (id, conditions, actions) VALUES ('a', 'not json', '[]')")
      val viewModel = database.createViewModel(this)

      viewModel.state.test {
        assertThat(awaitLoaded()).isInstanceOf(Failure::class)

        database.insertRule(id = "a")
        viewModel.retry()

        assertThat(awaitLoaded())
          .isInstanceOf(Success::class)
          .prop(Success::rules)
          .extracting { it.id.value }
          .containsExactly("a")
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  private suspend fun ReceiveTurbine<ListRulesState>.awaitLoaded(): ListRulesState {
    var state = awaitItem()
    while (state == Loading) state = awaitItem()
    return state
  }

  private suspend fun SqlDriver.run(sql: String) {
    execute(identifier = null, sql = sql, parameters = 0).await()
  }

  private suspend fun BudgetDatabase.insertRule(id: String) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId(id),
          stage = null,
          conditions = listOf(Condition(Notes, DoesNotContain, JsonPrimitive(id))),
          actions = listOf(RuleAction(value = null, op = DeleteTransaction)),
          tombstone = false,
          conditions_op = And,
        ),
      )

  private fun BudgetDatabase.createViewModel(scope: TestScope) =
    ListRulesViewModel(
      loader = RulesLoader(RulesDao(this)),
      syncController = TestSyncController(),
      nameFetcher = FakeNameFetcher,
      viewModelContext = scope.viewModelContext,
    )

  private object FakeNameFetcher : NameFetcher {
    override fun name(field: Field, id: String): Flow<String?> = emptyFlow()

    override fun name(id: ScheduleId): Flow<String?> = emptyFlow()

    override fun names(field: Field, ids: List<String>): Flow<JsonArray> = emptyFlow()
  }
}
