package aktual.budget.rules.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.rules.domain.ConditionEntitiesLoader
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.edit.EditRuleState.Loading
import aktual.budget.rules.vm.edit.EditRuleState.Success
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class EditRuleViewModelTest {
  @Test
  fun `Loads a saved rule`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      assertThat(awaitLoaded())
        .isEqualTo(Success(SAVED.toModel(), isWorking = false, persistentListOf(0L)))
    }
  }

  @Test
  fun `A missing rule fails`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope, RuleId("missing"))

    viewModel.state.test { assertThat(awaitLoaded()).isEqualTo(NotFound) }
  }

  @Test
  fun `A new rule for a payee matches that payee and sets a category`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope, payee = PayeeId("p"))

    viewModel.state.test {
      assertThat(awaitLoaded().rule()).all {
        prop(Rule::id).isEqualTo(RuleId("id-1"))
        prop(Rule::conditions)
          .isEqualTo(
            persistentListOf(Condition(Payee, Is, JsonPrimitive("p"), type = Id)),
          )
        prop(Rule::actions)
          .isEqualTo(
            persistentListOf(
              RuleAction(value = null, op = RuleAction.Op.Set, field = Category, type = Id),
            ),
          )
      }
    }
  }

  @Test
  fun `Conditions keep their keys as others come and go`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      viewModel.addCondition()
      val added = awaitItem() as Success
      assertThat(added.rule.conditions)
        .isEqualTo(persistentListOf(NOTES, Condition(Payee, Is, JsonNull, type = Id)))
      assertThat(added.conditionKeys).isEqualTo(persistentListOf(0L, 1L))

      val payee = Condition(Payee, Is, JsonPrimitive("p"), type = Id)
      viewModel.setCondition(1, payee)
      assertThat(awaitItem().rule().conditions).isEqualTo(persistentListOf(NOTES, payee))

      viewModel.removeCondition(0)
      val removed = awaitItem() as Success
      assertThat(removed.rule.conditions).isEqualTo(persistentListOf(payee))
      assertThat(removed.conditionKeys).isEqualTo(persistentListOf(1L))

      viewModel.setConditionsOp(Or)
      assertThat(awaitItem().rule().conditionsOp).isEqualTo(Or)

      viewModel.setStage(Post)
      assertThat(awaitItem().rule().stage).isEqualTo(Post)
    }
  }

  @Test
  fun `Saving writes the rule and syncs it`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, RuleId("a"), sync)

    viewModel.state.test {
      awaitLoaded()
      viewModel.setStage(Pre)
      viewModel.save()
      scope.advanceUntilIdle()
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(RulesDao(this)[RuleId("a")]).isNotNull().prop(Rules::stage).isEqualTo(Pre)
    assertThat(sync.changes.filter { it.dataset == RULES }).isNotEmpty()
  }

  @Test
  fun `Rules with unknown values can't be saved`() = runDatabaseTest { scope ->
    insertRule(SAVED.copy(stage = Unknown))
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      val state = awaitLoaded() as Success
      assertThat(state.canSave).isFalse()

      viewModel.setStage(Default)
      assertThat((awaitItem() as Success).canSave).isTrue()
    }
  }

  @Test
  fun `Deleting tombstones the rule and says so`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, RuleId("a"), sync)

    viewModel.events.test {
      viewModel.delete()
      assertThat(awaitItem()).isEqualTo(DeletedRule)
    }

    scope.advanceUntilIdle()
    assertThat(RulesDao(this).getAll()).isEmpty()
    assertThat(sync.changes.filter { it.dataset == RULES }).isNotEmpty()
  }

  private suspend fun ReceiveTurbine<EditRuleState>.awaitLoaded(): EditRuleState {
    var state = awaitItem()
    while (state == Loading) state = awaitItem()
    return state
  }

  private fun EditRuleState.rule(): Rule {
    assertThat(this).isInstanceOf(Success::class)
    return (this as Success).rule
  }

  private suspend fun BudgetDatabase.insertRule(rule: Rules) = RulesDao(this).insert(rule)

  private fun Rules.toModel() =
    Rule(
      id = id,
      stage = stage ?: Default,
      conditions = conditions.orEmpty().toImmutableList(),
      conditionsOp = conditions_op ?: Default,
      actions = actions.orEmpty().toImmutableList(),
    )

  private fun BudgetDatabase.createViewModel(
    scope: TestScope,
    ruleId: RuleId? = null,
    sync: TestSyncController = TestSyncController(),
    payee: PayeeId? = null,
  ): EditRuleViewModel {
    var nextId = 1
    return EditRuleViewModel(
      ruleId = ruleId,
      payee = payee,
      uuidGenerator = { "id-${nextId++}" },
      rulesDao = RulesDao(this),
      syncController = sync,
      nameFetcher = FakeNameFetcher,
      conditionEntitiesLoader = ConditionEntitiesLoader(this),
      viewModelContext = scope.viewModelContext,
    )
  }

  private object FakeNameFetcher : NameFetcher {
    override fun name(field: Field, id: String): Flow<String?> = emptyFlow()

    override fun name(id: ScheduleId): Flow<String?> = emptyFlow()

    override fun names(field: Field, ids: List<String>): Flow<JsonArray> = emptyFlow()
  }

  private companion object {
    val NOTES = Condition(Notes, Contains, JsonPrimitive("x"), type = ConditionType.String)

    val SAVED =
      Rules(
        id = RuleId("a"),
        stage = Default,
        conditions = listOf(NOTES),
        actions =
          listOf(
            RuleAction(
              value = JsonPrimitive("c"),
              op = RuleAction.Op.Set,
              field = Category,
              type = Id,
            ),
          ),
        tombstone = false,
        conditions_op = And,
      )
  }
}
