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
import aktual.budget.rules.domain.ACTION_OPS
import aktual.budget.rules.domain.ConditionEntitiesLoader
import aktual.budget.rules.domain.withField
import aktual.budget.rules.domain.withOp
import aktual.budget.rules.domain.withSplitMethod
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
        .isEqualTo(
          Success(
            SAVED.toModel(),
            isWorking = false,
            conditionKeys = persistentListOf(0L),
            actionKeys = persistentListOf(1L),
          ),
        )
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
      assertThat(added.conditionKeys).isEqualTo(persistentListOf(0L, 2L))

      val payee = Condition(Payee, Is, JsonPrimitive("p"), type = Id)
      viewModel.setCondition(1, payee)
      assertThat(awaitItem().rule().conditions).isEqualTo(persistentListOf(NOTES, payee))

      viewModel.removeCondition(0)
      val removed = awaitItem() as Success
      assertThat(removed.rule.conditions).isEqualTo(persistentListOf(payee))
      assertThat(removed.conditionKeys).isEqualTo(persistentListOf(2L))

      viewModel.setConditionsOp(Or)
      assertThat(awaitItem().rule().conditionsOp).isEqualTo(Or)

      viewModel.setStage(Post)
      assertThat(awaitItem().rule().stage).isEqualTo(Post)
    }
  }

  @Test
  fun `Actions keep their keys as others come and go`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      // Category is already set, so the next free field is picked
      viewModel.addAction()
      val added = awaitItem() as Success
      val payee = RuleAction(value = null, op = RuleAction.Op.Set, field = Payee, type = Id)
      assertThat(added.rule.actions).isEqualTo(persistentListOf(CATEGORY, payee))
      assertThat(added.actionKeys).isEqualTo(persistentListOf(1L, 2L))

      viewModel.removeAction(0)
      val removed = awaitItem() as Success
      assertThat(removed.rule.actions).isEqualTo(persistentListOf(payee))
      assertThat(removed.actionKeys).isEqualTo(persistentListOf(2L))
    }
  }

  @Test
  fun `Each action op can be set`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      // Reversed so the first change isn't to the op it already has
      var action = CATEGORY
      ACTION_OPS.reversed().forEach { op ->
        action = action.withOp(op)
        viewModel.setAction(0, action)
        assertThat(awaitItem().rule().actions).isEqualTo(persistentListOf(action))
      }
    }
  }

  @Test
  fun `Setting an action's field and value is kept`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      val notes = CATEGORY.withField(Notes).copy(value = JsonPrimitive("hello"))
      viewModel.setAction(0, notes)
      assertThat(awaitItem().rule().actions)
        .isEqualTo(
          persistentListOf(
            RuleAction(
              value = JsonPrimitive("hello"),
              op = RuleAction.Op.Set,
              field = Notes,
              type = RuleAction.Type.String,
            ),
          ),
        )
    }
  }

  @Test
  fun `Actions added to a split belong to it`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      viewModel.addAction(splitIndex = 1)
      assertThat(awaitItem().rule().actions[1])
        .isEqualTo(
          RuleAction(
            value = null,
            op = RuleAction.Op.Set,
            field = Category,
            type = Id,
            options = RuleAction.Options(splitIndex = 1),
          ),
        )
    }
  }

  @Test
  fun `A new split takes the remainder and sets a category`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      viewModel.addSplit()
      assertThat(awaitItem() as Success).all {
        prop(Success::rule)
          .prop(Rule::actions)
          .isEqualTo(persistentListOf(CATEGORY, remainder(1), splitCategory(1)))
        prop(Success::actionKeys).isEqualTo(persistentListOf(1L, 2L, 3L))
      }

      val fixed = remainder(1).withSplitMethod(FixedAmount).copy(value = JsonPrimitive(2000))
      viewModel.setAction(1, fixed)
      assertThat(awaitItem().rule().actions)
        .isEqualTo(persistentListOf(CATEGORY, fixed, splitCategory(1)))
    }
  }

  @Test
  fun `Actions added to an earlier split go after its last action`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()
      viewModel.addSplit()
      awaitItem()

      viewModel.addAction(splitIndex = 0)
      val payee = RuleAction(value = null, op = RuleAction.Op.Set, field = Payee, type = Id)
      assertThat(awaitItem() as Success).all {
        prop(Success::rule)
          .prop(Rule::actions)
          .isEqualTo(persistentListOf(CATEGORY, payee, remainder(1), splitCategory(1)))
        prop(Success::actionKeys).isEqualTo(persistentListOf(1L, 4L, 2L, 3L))
      }
    }
  }

  @Test
  fun `Removing a split moves later ones down with their keys`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()
      viewModel.addSplit()
      awaitItem()
      viewModel.addSplit()
      awaitItem()

      viewModel.removeSplit(1)
      assertThat(awaitItem() as Success).all {
        prop(Success::rule)
          .prop(Rule::actions)
          .isEqualTo(persistentListOf(CATEGORY, remainder(1), splitCategory(1)))
        prop(Success::actionKeys).isEqualTo(persistentListOf(1L, 4L, 5L))
      }

      // The whole transaction stays
      viewModel.removeSplit(0)
      expectNoEvents()
    }
  }

  @Test
  fun `Edits to a removed action or condition are dropped`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()

      viewModel.setAction(1, CATEGORY)
      viewModel.setCondition(1, NOTES)
      expectNoEvents()
    }
  }

  @Test
  fun `A new rule sets a category`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThat(awaitLoaded().rule().actions)
        .isEqualTo(
          persistentListOf(
            RuleAction(value = null, op = RuleAction.Op.Set, field = Category, type = Id),
          ),
        )
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

    val CATEGORY =
      RuleAction(value = JsonPrimitive("c"), op = RuleAction.Op.Set, field = Category, type = Id)

    val SAVED =
      Rules(
        id = RuleId("a"),
        stage = Default,
        conditions = listOf(NOTES),
        actions = listOf(CATEGORY),
        tombstone = false,
        conditions_op = And,
      )

    fun remainder(split: Int) =
      RuleAction(
        value = null,
        op = SetSplitAmount,
        options = RuleAction.Options(method = Remainder, splitIndex = split),
      )

    fun splitCategory(split: Int) =
      RuleAction(
        value = null,
        op = RuleAction.Op.Set,
        field = Category,
        type = Id,
        options = RuleAction.Options(splitIndex = split),
      )
  }
}
