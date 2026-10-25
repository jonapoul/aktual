package aktual.budget.rules.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.DbJson
import aktual.budget.db.Rules
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryLearningDao
import aktual.budget.db.dao.LearningTransaction
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.RuleId
import aktual.budget.model.SyncedPrefKey.Global.LearnCategories
import aktual.budget.model.TransactionId
import aktual.test.TestCalendar
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class CategoryLearnerTest {
  @Test
  fun `The most common category needs at least 3 of the latest`() {
    assertThat(probableCategory(categories(listOf(FOOD)))).isNull()
    assertThat(probableCategory(categories(listOf(FOOD, FOOD, FOOD)))).isEqualTo(FOOD)
    assertThat(probableCategory(categories(listOf(BEER, FOOD, FOOD, FOOD)))).isEqualTo(FOOD)
    assertThat(probableCategory(categories(listOf(BEER, BEER, BEER, FOOD, FOOD, FOOD))))
      .isEqualTo(BEER)
  }

  @Test
  fun `An uncategorised newest transaction stops learning`() {
    assertThat(probableCategory(categories(listOf(null, FOOD, FOOD, FOOD)))).isNull()
    assertThat(probableCategory(categories(listOf(FOOD, null, FOOD, FOOD)))).isEqualTo(FOOD)
  }

  @Test
  fun `Adds a rule once a category wins`() = runLearnerTest {
    // given
    transaction("t1", day = 1, category = FOOD)
    transaction("t2", day = 2, category = FOOD)

    // when
    learner.learn(ids("t2"))

    // then
    assertThat(sync.changes).isEmpty()

    // when
    transaction("t3", day = 3, category = FOOD)
    learner.learn(ids("t3"))

    // then
    assertThat(sentRules())
      .isEqualTo(
        mapOf(
          "new-rule" to
            SentRule(
              conditions = listOf(isPayee(FOO)),
              actions = listOf(setCategory(FOOD)),
            ),
        ),
      )
  }

  @Test
  fun `Only learns when a changed transaction is among the latest 5`() = runLearnerTest {
    // given
    transaction("old", day = 1, category = BEER)
    for (day in 2..6) transaction("t$day", day = day, category = FOOD)

    // when
    learner.learn(ids("old"))

    // then
    assertThat(sync.changes).isEmpty()
  }

  @Test
  fun `Points existing rules for the payee at the winner`() = runLearnerTest {
    // given
    rule("r1", condition = isPayee(FOO), action = setCategory(BEER))
    rule("r2", condition = isPayee(FOO), action = setCategory(FUN))
    rule("same", condition = isPayee(FOO), action = setCategory(FOOD))
    rule("other-payee", condition = isPayee(BAR), action = setCategory(BEER))
    for (day in 1..3) transaction("t$day", day = day, category = FOOD)

    // when
    learner.learn(ids("t3"))

    // then
    assertThat(sync.changes.map { it.row to it.column })
      .containsExactlyInAnyOrder("r1" to "actions", "r2" to "actions")
    assertThat(sentRules().mapValues { it.value.actions })
      .isEqualTo(mapOf("r1" to listOf(setCategory(FOOD)), "r2" to listOf(setCategory(FOOD))))
  }

  @Test
  fun `A oneOf rule already setting the winner stops a new rule`() = runLearnerTest {
    // given
    rule("one-of", condition = oneOfPayees(listOf(FOO, BAR)), action = setCategory(FOOD))
    for (day in 1..3) transaction("t$day", day = day, category = FOOD)

    // when
    learner.learn(ids("t3"))

    // then
    assertThat(sync.changes).isEmpty()
  }

  @Test
  fun `A oneOf rule setting something else gets a new rule alongside`() = runLearnerTest {
    // given
    rule("one-of", condition = oneOfPayees(listOf(FOO, BAR)), action = setCategory(BEER))
    for (day in 1..3) transaction("t$day", day = day, category = FOOD)

    // when
    learner.learn(ids("t3"))

    // then
    assertThat(sentRules().keys).isEqualTo(setOf("new-rule"))
  }

  @Test
  fun `Closed accounts and payees that don't learn are left out`() = runLearnerTest {
    // given
    database.accountsQueries.withoutResult { closeAccount(CLOSED) }
    payeeDao.update(BAR, name = "bar", favorite = false, learnCategories = false)
    for (day in 1..3) transaction("closed$day", day = day, category = FOOD, account = CLOSED)
    for (day in 1..3) transaction("bar$day", day = day, category = FOOD, payee = BAR)

    // when
    learner.learn(ids("closed3", "bar3"))

    // then
    assertThat(sync.changes).isEmpty()
  }

  @Test
  fun `Does nothing while the pref is off`() = runLearnerTest {
    // given
    preferences[LearnCategories] = "false"
    for (day in 1..3) transaction("t$day", day = day, category = FOOD)

    // when
    learner.learn(ids("t3"))

    // then
    assertThat(sync.changes).isEmpty()
  }

  private fun categories(categories: List<CategoryId?>) = categories.mapIndexed { i, category ->
    LearningTransaction(TransactionId("t$i"), FOO, category)
  }

  private fun ids(vararg ids: String) = ids.map(::TransactionId)

  private fun isPayee(payee: PayeeId) =
    Condition(field = Payee, operator = Is, type = Id, value = JsonPrimitive(payee.value))

  private fun oneOfPayees(payees: List<PayeeId>) =
    Condition(
      field = Payee,
      operator = OneOf,
      type = Id,
      value = JsonArray(payees.map { JsonPrimitive(it.value) }),
    )

  private fun setCategory(category: CategoryId) =
    RuleAction(value = JsonPrimitive(category.value), op = Set, field = Category, type = Id)

  private data class SentRule(val conditions: List<Condition>?, val actions: List<RuleAction>?)

  private class LearnerScope(val database: BudgetDatabase, scope: TestScope) {
    val sync = TestSyncController()
    val preferences =
      PreferencesDao(database, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
    val payeeDao = PayeeDao(database)
    private val transactionDao = TransactionDao(database)

    val learner =
      CategoryLearnerImpl(
        learningDao = CategoryLearningDao(database),
        rulesDao = RulesDao(database),
        pref = CategoryLearningPref(preferences, sync),
        syncController = sync,
        uuidGenerator = { "new-rule" },
        calendar = TestCalendar(TODAY),
      )

    suspend fun transaction(
      id: String,
      day: Int,
      category: CategoryId,
      payee: PayeeId = FOO,
      account: AccountId = OPEN,
    ) =
      transactionDao.insert(
        id = id,
        account = account.value,
        category = category.value,
        payee = payee.value,
        date = LocalDate(2026, 10, day),
      )

    suspend fun rule(id: String, condition: Condition, action: RuleAction) =
      RulesDao(database)
        .insert(
          Rules(
            id = RuleId(id),
            stage = null,
            conditions = listOf(condition),
            actions = listOf(action),
            tombstone = false,
            conditions_op = And,
          ),
        )

    // The rule columns sent, by row
    fun sentRules(): Map<String, SentRule> =
      sync.changes
        .groupBy { it.row }
        .mapValues { (_, changes) ->
          fun json(column: String) =
            (changes.firstOrNull { it.column == column }?.value as? MessageValue.String)?.value
          SentRule(
            conditions = json("conditions")?.let { DbJson.decodeFromString(it) },
            actions = json("actions")?.let { DbJson.decodeFromString(it) },
          )
        }

    suspend fun setUp() {
      AccountDao(database).insert(id = OPEN, name = "open")
      AccountDao(database).insert(id = CLOSED, name = "closed")
      for (payee in listOf(FOO, BAR)) {
        payeeDao.insert(payee, name = payee.value)
        database.payeeMappingQueries.withoutResult { insert(payee, payee) }
      }
      for (category in listOf(FOOD, BEER, FUN)) {
        database.categoryMappingQueries.withoutResult { insert(category, category) }
        database.categoriesQueries.withoutResult {
          insert(
            Categories(
              id = category,
              name = category.value,
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
        }
      }
    }
  }

  private fun runLearnerTest(action: suspend LearnerScope.() -> Unit) = runDatabaseTest { scope ->
    val learnerScope = LearnerScope(this, scope)
    learnerScope.setUp()
    learnerScope.action()
  }

  private companion object {
    val TODAY = LocalDate(2026, 10, 25)
    val OPEN = AccountId("open")
    val CLOSED = AccountId("closed")
    val FOO = PayeeId("foo")
    val BAR = PayeeId("bar")
    val FOOD = CategoryId("food")
    val BEER = CategoryId("beer")
    val FUN = CategoryId("fun")
  }
}
