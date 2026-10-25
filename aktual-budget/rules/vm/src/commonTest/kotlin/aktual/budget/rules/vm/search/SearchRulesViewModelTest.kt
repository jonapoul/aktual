package aktual.budget.rules.vm.search

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.RuleSearchText
import aktual.budget.rules.vm.RulesLoader
import aktual.budget.rules.vm.StringLookup
import aktual.budget.rules.vm.search.SearchRulesState.Results
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.jetbrains.compose.resources.StringResource

class SearchRulesViewModelTest {
  @Test
  fun `Blank query prompts for input`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(notesDoNotContain("rent")))
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(NoQuery)
      viewModel.setQuery("   ")
      scope.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `Matches resolved entity names case-insensitively`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(Condition(Payee, Is, JsonPrimitive("p1"))))
    insertRule(id = "b", conditions = listOf(Condition(Payee, OneOf, ids("p2", "p3"))))
    insertRule(id = "c", conditions = listOf(notesDoNotContain("other")))
    val viewModel = createViewModel(scope)

    viewModel.setQuery(" LANDLORD ")

    viewModel.state.test {
      assertThat(awaitResults()).extracting { it.id.value }.containsExactly("a", "b")
    }
  }

  @Test
  fun `Matches field and operator labels`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(notesDoNotContain("x")))
    insertRule(id = "b", conditions = listOf(Condition(Payee, Is, JsonPrimitive("p1"))))
    val viewModel = createViewModel(scope)

    viewModel.setQuery("does not contain")

    viewModel.state.test {
      assertThat(awaitResults()).extracting { it.id.value }.containsExactly("a")
    }
  }

  @Test
  fun `Matches action values`() = runDatabaseTest { scope ->
    insertRule(
      id = "a",
      conditions = listOf(notesDoNotContain("x")),
      actions = listOf(RuleAction(value = JsonPrimitive("Monthly bill"), op = AppendNotes)),
    )
    insertRule(
      id = "b",
      conditions = listOf(notesDoNotContain("x")),
      actions = listOf(RuleAction(value = JsonPrimitive("s1"), op = LinkSchedule)),
    )
    insertRule(
      id = "c",
      conditions = listOf(notesDoNotContain("x")),
      actions =
        listOf(RuleAction(value = JsonPrimitive("c1"), op = RuleAction.Op.Set, field = Category)),
    )
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      viewModel.setQuery("bill")
      assertThat(awaitResults()).extracting { it.id.value }.containsExactly("a", "b")

      viewModel.setQuery("groceries")
      assertThat(awaitResults()).extracting { it.id.value }.containsExactly("c")
    }
  }

  @Test
  fun `No matches shows no results`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(notesDoNotContain("rent")))
    val viewModel = createViewModel(scope)

    viewModel.setQuery("xyz")

    viewModel.state.test {
      var state = awaitItem()
      while (state != NoResults) state = awaitItem()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `A payee filter lists its rules without a query`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(Condition(Payee, Is, JsonPrimitive("p1"))))
    insertRule(id = "b", conditions = listOf(Condition(Payee, OneOf, ids("p2", "p3"))))
    insertRule(
      id = "c",
      conditions = listOf(notesDoNotContain("x")),
      actions =
        listOf(RuleAction(value = JsonPrimitive("p3"), op = RuleAction.Op.Set, field = Payee)),
    )
    val viewModel = createViewModel(scope, payee = PayeeId("p3"))

    viewModel.payeeName.test { assertThat(awaitNonNull()).isEqualTo("landlord ltd") }
    viewModel.state.test {
      assertThat(awaitResults()).extracting { it.id.value }.containsExactly("b", "c")
    }
  }

  @Test
  fun `A payee filter narrows the query matches`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(Condition(Payee, Is, JsonPrimitive("p1"))))
    insertRule(id = "b", conditions = listOf(Condition(Payee, Is, JsonPrimitive("p3"))))
    val viewModel = createViewModel(scope, payee = PayeeId("p1"))

    viewModel.setQuery("landlord")

    viewModel.state.test {
      assertThat(awaitResults()).extracting { it.id.value }.containsExactly("a")
    }
  }

  @Test
  fun `Clearing the payee filter prompts for input`() = runDatabaseTest { scope ->
    insertRule(id = "a", conditions = listOf(Condition(Payee, Is, JsonPrimitive("p1"))))
    val viewModel = createViewModel(scope, payee = PayeeId("p1"))

    viewModel.state.test {
      awaitResults()
      viewModel.clearPayee()
      var state = awaitItem()
      while (state != NoQuery) state = awaitItem()
    }
    viewModel.payeeName.test {
      var name = awaitItem()
      while (name != null) name = awaitItem()
    }
  }

  private suspend fun ReceiveTurbine<String?>.awaitNonNull(): String {
    var name = awaitItem()
    while (name == null) name = awaitItem()
    return name
  }

  private suspend fun ReceiveTurbine<SearchRulesState>.awaitResults(): List<Rule> {
    var state = awaitItem()
    while (state !is Results) state = awaitItem()
    return state.rules
  }

  private fun notesDoNotContain(value: String) =
    Condition(Notes, DoesNotContain, JsonPrimitive(value))

  private fun ids(vararg ids: String) = buildJsonArray { ids.forEach { add(JsonPrimitive(it)) } }

  private suspend fun BudgetDatabase.insertRule(
    id: String,
    conditions: List<Condition>,
    actions: List<RuleAction> = listOf(RuleAction(value = null, op = DeleteTransaction)),
  ) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId(id),
          stage = null,
          conditions = conditions,
          actions = actions,
          tombstone = false,
          conditions_op = And,
        ),
      )

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    payee: PayeeId? = null,
  ): SearchRulesViewModel {
    return SearchRulesViewModel(
      initialPayee = payee,
      savedState = SavedStateHandle(),
      loader = RulesLoader(RulesDao(this)),
      searchText = RuleSearchText(FakeNameFetcher, FakeStringLookup),
      nameFetcher = FakeNameFetcher,
      viewModelContext = scope.viewModelContext,
    )
  }

  private object FakeNameFetcher : NameFetcher {
    private val names =
      mapOf("p1" to "Landlord", "p2" to "Gym", "p3" to "landlord ltd", "c1" to "Groceries")

    override fun name(field: Field, id: String): Flow<String?> = flowOf(names[id])

    override fun name(id: ScheduleId): Flow<String?> = flowOf("Phone bill")

    override fun names(field: Field, ids: List<String>): Flow<JsonArray> =
      flowOf(
        buildJsonArray { ids.forEach { add(JsonPrimitive(names[it] ?: it)) } },
      )
  }

  // "rules_operator_does_not_contain" -> "does not contain"
  private object FakeStringLookup : StringLookup {
    override suspend fun get(resource: StringResource): String =
      resource.key.substringAfter("rules_").substringAfter('_').replace('_', ' ')
  }
}
