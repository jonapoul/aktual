package aktual.budget.rules.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.Rules
import aktual.budget.db.Transactions
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.ACTION_OPS
import aktual.budget.rules.domain.ConditionEntitiesLoaderImpl
import aktual.budget.rules.domain.NameFetcher
import aktual.budget.rules.domain.Rule
import aktual.budget.rules.domain.RulesLoaderImpl
import aktual.budget.rules.domain.RulesWriterImpl
import aktual.budget.rules.domain.TransactionRulesLoaderImpl
import aktual.budget.rules.domain.withField
import aktual.budget.rules.domain.withOp
import aktual.budget.rules.domain.withSplitMethod
import aktual.budget.rules.vm.edit.EditRuleState.Loading
import aktual.budget.rules.vm.edit.EditRuleState.Success
import aktual.budget.schedules.domain.SchedulesLoaderImpl
import aktual.budget.transactions.domain.RuleMatcherImpl
import aktual.budget.transactions.domain.TransactionWriterImpl
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import aktual.test.viewModelContext
import alakazam.test.TestCoroutineContexts
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
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
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

      viewModel.addCondition(Condition(Payee, Is, JsonNull, type = Id))
      val added = awaitItem() as Success
      assertThat(added.rule.conditions)
        .isEqualTo(persistentListOf(NOTES, Condition(Payee, Is, JsonNull, type = Id)))
      assertThat(added.conditionKeys).isEqualTo(persistentListOf(0L, 2L))

      val payee = Condition(Payee, Is, JsonPrimitive("p"), type = Id)
      viewModel.setCondition(2L, payee)
      assertThat(awaitItem().rule().conditions).isEqualTo(persistentListOf(NOTES, payee))

      viewModel.removeCondition(0L)
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

      val payee = RuleAction(value = null, op = RuleAction.Op.Set, field = Payee, type = Id)
      viewModel.addAction(payee)
      val added = awaitItem() as Success
      assertThat(added.rule.actions).isEqualTo(persistentListOf(CATEGORY, payee))
      assertThat(added.actionKeys).isEqualTo(persistentListOf(1L, 2L))

      viewModel.removeAction(1L)
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
        viewModel.setAction(1L, action)
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
      viewModel.setAction(1L, notes)
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

      val action =
        RuleAction(
          value = null,
          op = RuleAction.Op.Set,
          field = Category,
          type = Id,
          options = RuleAction.Options(splitIndex = 1),
        )
      viewModel.addAction(action)
      assertThat(awaitItem().rule().actions[1]).isEqualTo(action)
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
      viewModel.setAction(2L, fixed)
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

      val payee = RuleAction(value = null, op = RuleAction.Op.Set, field = Payee, type = Id)
      viewModel.addAction(payee)
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

      viewModel.setAction(99L, CATEGORY)
      viewModel.setCondition(99L, NOTES)
      viewModel.removeAction(99L)
      viewModel.removeCondition(99L)
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
  fun `Saving says so`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      awaitLoaded()
      viewModel.setStage(Pre)
      viewModel.events.test {
        viewModel.save()
        assertThat(awaitItem()).isEqualTo(Saved)
      }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Changes are tracked against the saved rule`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      assertThat(awaitLoaded() as Success).all {
        prop(Success::hasChanges).isFalse()
        prop(Success::canSave).isFalse()
      }

      viewModel.setStage(Pre)
      assertThat(awaitItem() as Success).all {
        prop(Success::hasChanges).isTrue()
        prop(Success::canSave).isTrue()
      }

      viewModel.setStage(Default)
      assertThat((awaitItem() as Success).hasChanges).isFalse()

      viewModel.setStage(Post)
      assertThat((awaitItem() as Success).hasChanges).isTrue()

      viewModel.save()
      scope.advanceUntilIdle()
      assertThat((expectMostRecentItem() as Success).hasChanges).isFalse()
    }
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

  @Test
  fun `A schedule's rule can't be deleted`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    insertSchedule(id = "s", rule = "a")
    val sync = TestSyncController()
    val viewModel = createViewModel(scope, RuleId("a"), sync)

    viewModel.state.test {
      assertThat(awaitLoaded() as Success).all {
        prop(Success::schedule).isEqualTo(ScheduleId("s"))
        prop(Success::canDelete).isFalse()
      }
    }

    viewModel.delete()
    scope.advanceUntilIdle()
    assertThat(RulesDao(this).getAll()).isNotEmpty()
    assertThat(sync.changes).isEmpty()
  }

  @Test
  fun `Linking a schedule by hand still lets the rule be deleted`() = runDatabaseTest { scope ->
    insertRule(SAVED.copy(actions = listOf(LINK)))
    insertSchedule(id = "s", rule = "other")
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test {
      assertThat(awaitLoaded() as Success).all {
        prop(Success::schedule).isNull()
        prop(Success::canDelete).isTrue()
      }
    }
  }

  @Test
  fun `Schedules are null until they've loaded`() = runDatabaseTest { scope ->
    insertSchedule(id = "s", rule = "a")
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.schedules.test {
      assertThat(awaitItem()).isNull()
      assertThat(awaitItem()).isNotNull()
    }
  }

  @Test
  fun `A tombstoned schedule doesn't own its rule`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    insertSchedule(id = "s", rule = "a", tombstone = true)
    val viewModel = createViewModel(scope, RuleId("a"))

    viewModel.state.test { assertThat((awaitLoaded() as Success).canDelete).isTrue() }
  }

  @Test
  fun `Matches load once the conditions settle`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)

    viewModel.matches.test {
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loading)
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loaded(0, 0, persistentListOf()))
    }
  }

  @Test
  fun `Matches are searched for again when shown again`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)

    viewModel.matches.test {
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loading)
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loaded(0, 0, persistentListOf()))
    }

    // Lets the search stop now nothing's watching, as it would while the sheet is closed
    scope.advanceUntilIdle()

    viewModel.matches.test {
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loading)
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loaded(0, 0, persistentListOf()))
    }
  }

  @Test
  fun `Ticked matches are forgotten when shown again`() = runDatabaseTest { scope ->
    insertRule(SAVED)
    insertTransaction(id = "t", notes = "x")
    val viewModel = createViewModel(scope, RuleId("a"))
    viewModel.state.test { awaitLoaded() }

    viewModel.matches.test {
      assertThat(awaitLoadedMatches()).all {
        prop(RuleMatchesState.Loaded::changing).isEqualTo(1)
        prop(RuleMatchesState.Loaded::selected).isEmpty()
      }
      viewModel.toggleMatch(TransactionId("t"))
      assertThat(awaitItem())
        .isInstanceOf<RuleMatchesState.Loaded>()
        .prop(RuleMatchesState.Loaded::selected)
        .isNotEmpty()
    }

    scope.advanceUntilIdle()

    viewModel.matches.test { assertThat(awaitLoadedMatches().selected).isEmpty() }
  }

  @Test
  fun `Matches can't show invalid actions`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope)

    viewModel.matches.test {
      assertThat(awaitItem()).isEqualTo(RuleMatchesState.Loading)
      skipItems(1)
      viewModel.setAction(1L, CATEGORY.copy(field = Account, value = JsonPrimitive("")))
      assertThat(awaitItem()).isEqualTo(Invalid)
    }
  }

  private suspend fun ReceiveTurbine<RuleMatchesState>.awaitLoadedMatches():
    RuleMatchesState.Loaded {
    var state = awaitItem()
    while (state !is Loaded) state = awaitItem()
    return state
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
      rulesLoader = RulesLoaderImpl(RulesDao(this)),
      rulesWriter = RulesWriterImpl(RulesDao(this), sync),
      ruleMatcher =
        RuleMatcherImpl(
          transactionDao = TransactionDao(this),
          accountDao = AccountDao(this),
          categoryDao = CategoryDao(this),
          payeeDao = PayeeDao(this),
          rulesLoader =
            TransactionRulesLoaderImpl(RulesDao(this), RuleContextDao(this)) { "id-${nextId++}" },
          writer =
            TransactionWriterImpl(
              syncController = sync,
              accountDao = AccountDao(this),
              categoryDao = CategoryDao(this),
              payeeDao = PayeeDao(this),
              transactionDao = TransactionDao(this),
              categoryLearner = {},
              uuidGenerator = { "id-${nextId++}" },
              clock = Clock.System,
            ),
          uuidGenerator = { "id-${nextId++}" },
        ),
      nameFetcher = FakeNameFetcher,
      conditionEntitiesLoader = ConditionEntitiesLoaderImpl(this),
      schedulesLoader =
        SchedulesLoaderImpl(
          scheduleDao = ScheduleDao(this),
          accountDao = AccountDao(this),
          payeeDao = PayeeDao(this),
          preferencesDao =
            PreferencesDao(
              this,
              TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)),
            ),
          calendar = { LocalDate(2026, 4, 1) },
        ),
      viewModelContext = scope.viewModelContext,
    )
  }

  private suspend fun BudgetDatabase.insertTransaction(id: String, notes: String) {
    AccountDao(this).insert(id = AccountId("acc"), name = "Account", offBudget = false)
    // The category CATEGORY sets, so the rule changes the transaction
    categoryMappingQueries.insert(CategoryId("c"), CategoryId("c"))
    categoriesQueries.insert(
      Categories(
        id = CategoryId("c"),
        name = "Groceries",
        is_income = false,
        cat_group = null,
        sort_order = null,
        tombstone = false,
        hidden = false,
        goal_def = null,
        template_settings = null,
        cleanup_def = null,
      ),
    )
    transactionsQueries.withoutResult {
      insert(
        Transactions(
          id = TransactionId(id),
          isParent = false,
          isChild = false,
          acct = AccountId("acc"),
          category = null,
          amount = Amount(-1000),
          description = null,
          notes = notes,
          date = LocalDate(2026, 1, 1),
          financial_id = null,
          type = null,
          location = null,
          error = null,
          imported_description = null,
          starting_balance_flag = false,
          transferred_id = null,
          sort_order = null,
          tombstone = false,
          cleared = true,
          pending = false,
          parent_id = null,
          schedule = null,
          reconciled = false,
          raw_synced_data = null,
        ),
      )
    }
  }

  private suspend fun BudgetDatabase.insertSchedule(
    id: String,
    rule: String,
    tombstone: Boolean = false,
  ) = schedulesQueries.withoutResult {
    insert(
      id = ScheduleId(id),
      rule = RuleId(rule),
      active = true,
      completed = false,
      posts_transaction = false,
      tombstone = tombstone,
      name = id,
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

    val LINK = RuleAction(value = JsonPrimitive("s"), op = LinkSchedule)

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
