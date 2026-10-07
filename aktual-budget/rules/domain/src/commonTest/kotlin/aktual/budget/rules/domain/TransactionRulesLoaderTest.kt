package aktual.budget.rules.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Categories
import aktual.budget.db.Rules
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.withoutResult
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class TransactionRulesLoaderTest {
  @Test
  fun `Loads rules and lookups from the database`() = runDatabaseTest {
    insertAccount(ACCOUNT, offBudget = false)
    insertAccount(OFF_BUDGET_ACCOUNT, offBudget = true)
    insertPayee(PayeeId("kroger"), "Kroger")
    insertPayee(PayeeId("old-kroger"), "Old Kroger", tombstone = true)
    payeeMappingQueries.withoutResult { insert(PayeeId("old-kroger"), PayeeId("kroger")) }
    insertCategory(CategoryId("groceries"), CategoryGroupId("food"))

    // Stored with the transactions table's column names, as upstream saves rules
    insertRule(
      "rename",
      stage = Pre,
      conditions = listOf(cond(ImportedDescription, Contains, "kroger")),
      actions = listOf(set(Description, "old-kroger")),
    )
    insertRule(
      "categorise",
      conditions = listOf(cond(PayeeName, Is, "kroger"), cond(Account, OnBudget, null)),
      actions = listOf(set(Category, "groceries")),
    )
    insertRule(
      "group",
      stage = Post,
      conditions = listOf(cond(CategoryGroup, Is, "food")),
      actions = listOf(action(AppendNotes, value = " #food")),
    )
    insertRule(
      "deleted",
      conditions = listOf(cond(Notes, Is, "")),
      actions = listOf(set(Notes, "deleted rule ran")),
      tombstone = true,
    )
    insertRule(
      "invalid",
      conditions = listOf(cond(Notes, Contains, "")),
      actions = emptyList(),
    )
    schedulesQueries.withoutResult {
      insert(
        id = ScheduleId("schedule"),
        rule = RuleId("group"),
        active = true,
        completed = false,
        posts_transaction = false,
        tombstone = false,
        name = "Schedule",
      )
      insert(
        id = ScheduleId("other"),
        rule = RuleId("categorise"),
        active = true,
        completed = false,
        posts_transaction = false,
        tombstone = false,
        name = "Other",
      )
    }

    val loaded = loader(this).load()

    assertThat(loaded.engine.rankedRules.map(RuleId::value))
      .containsExactly("rename", "categorise", "group")
    assertThat(loaded.engine.invalidRules).containsExactly(RuleId("invalid"))

    // The merged payee's id is swapped for the payee it was merged into
    val renamed = loaded.run(tx(importedPayee = "KROGER #123"))
    assertThat(renamed.payee).isEqualTo(PayeeId("kroger"))

    val onBudget = loaded.run(tx(payee = "kroger", notes = "Shopping"))
    assertThat(onBudget.category).isEqualTo(CategoryId("groceries"))
    assertThat(onBudget.notes).isEqualTo("Shopping #food")

    val offBudget =
      loaded.run(tx(payee = "kroger", notes = "Shopping", account = OFF_BUDGET_ACCOUNT))
    assertThat(offBudget.category).isNull()
    assertThat(offBudget.notes).isEqualTo("Shopping")

    // A transaction from a schedule runs its rule whatever the conditions, and skips rules
    // belonging to other schedules
    val fromSchedule = loaded.run(tx(notes = "x").copy(schedule = ScheduleId("other")))
    assertThat(fromSchedule.category).isEqualTo(CategoryId("groceries"))
    assertThat(fromSchedule.notes).isEqualTo("x")
  }

  @Test
  fun `Payees created by rules are collected`() = runDatabaseTest {
    insertAccount(ACCOUNT, offBudget = false)
    insertRule(
      "rename",
      conditions = listOf(cond(ImportedPayee, Contains, "tesco")),
      actions = listOf(set(PayeeName, "Tesco")),
    )

    val loaded = loader(this).load()
    val result = loaded.run(tx(importedPayee = "TESCO STORES"))

    assertThat(result.payee).isEqualTo(PayeeId("uuid-1"))
    assertThat(loaded.createdPayees).isEqualTo(mapOf(PayeeId("uuid-1") to "Tesco"))
  }

  private fun loader(database: BudgetDatabase): TransactionRulesLoader {
    var count = 0
    return TransactionRulesLoader(
      rulesDao = RulesDao(database),
      contextDao = RuleContextDao(database),
      uuidGenerator = { "uuid-${++count}" },
    )
  }

  private suspend fun BudgetDatabase.insertAccount(id: AccountId, offBudget: Boolean) =
    accountsQueries.withoutResult {
      insert(id, null, id.value, null, null, offBudget, null)
    }

  private suspend fun BudgetDatabase.insertPayee(
    id: PayeeId,
    name: String,
    tombstone: Boolean = false,
  ) = payeesQueries.withoutResult { insert(id, name, null, tombstone, null, false, null) }

  private suspend fun BudgetDatabase.insertCategory(id: CategoryId, group: CategoryGroupId) =
    categoriesQueries.withoutResult {
      insert(
        Categories(
          id = id,
          name = id.value,
          is_income = false,
          cat_group = group,
          sort_order = null,
          tombstone = false,
          hidden = false,
          goal_def = null,
          template_settings = null,
          cleanup_def = null,
        ),
      )
    }

  private suspend fun BudgetDatabase.insertRule(
    id: String,
    conditions: List<Condition>,
    actions: List<RuleAction>,
    stage: RuleStage? = null,
    tombstone: Boolean = false,
  ) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId(id),
          stage = stage,
          conditions = conditions,
          actions = actions,
          tombstone = tombstone,
          conditions_op = And,
        ),
      )
}
