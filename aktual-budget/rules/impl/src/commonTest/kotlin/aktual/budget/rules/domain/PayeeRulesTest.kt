package aktual.budget.rules.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.Rules
import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.withoutResult
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test

class PayeeRulesTest {
  @Test
  fun `Payee ids come from single and list conditions`() {
    val rule =
      rule(
        conditions =
          listOf(
            cond(Payee, Is, "a"),
            cond(Payee, IsNot, "b"),
            cond(Payee, OneOf, listOf("c", "d")),
            cond(Payee, NotOneOf, listOf("e")),
          ),
      )

    assertThat(rule.payeeIds())
      .containsExactlyInAnyOrder(
        PayeeId("a"),
        PayeeId("b"),
        PayeeId("c"),
        PayeeId("d"),
        PayeeId("e"),
      )
  }

  @Test
  fun `Payee ids include stored description fields and set payee actions`() {
    val rule =
      rule(
        conditions = listOf(cond(Description, Is, "a")),
        actions = listOf(set(Description, "b"), set(Payee, "c")),
      )

    assertThat(rule.payeeIds()).containsExactlyInAnyOrder(PayeeId("a"), PayeeId("b"), PayeeId("c"))
  }

  @Test
  fun `Payee ids leave out text matches and other fields`() {
    val rule =
      rule(
        conditions =
          listOf(
            cond(Payee, Contains, "a"),
            cond(Payee, Matches, "b"),
            cond(PayeeName, Is, "c"),
            cond(Category, Is, "d"),
          ),
        actions = listOf(set(Category, "e"), action(AppendNotes, value = "f")),
      )

    assertThat(rule.payeeIds()).isEmpty()
  }

  @Test
  fun `Counts each rule once per payee`() = runDatabaseTest {
    insertRule("r1", conditions = listOf(cond(Payee, Is, "a")), actions = listOf(set(Payee, "a")))
    insertRule("r2", conditions = listOf(cond(Payee, OneOf, listOf("a", "b"))))

    assertThat(counter().count()).isEqualTo(mapOf(PayeeId("a") to 2, PayeeId("b") to 1))
  }

  @Test
  fun `Counts leave out deleted rules and rules of completed schedules`() = runDatabaseTest {
    insertRule("active", conditions = listOf(cond(Payee, Is, "a")))
    insertRule("completed", conditions = listOf(cond(Payee, Is, "a")))
    insertRule("deleted", conditions = listOf(cond(Payee, Is, "a")), tombstone = true)
    insertRule("deleted-schedule", conditions = listOf(cond(Payee, Is, "a")))
    insertSchedule("s1", rule = "active", completed = false)
    insertSchedule("s2", rule = "completed", completed = true)
    insertSchedule("s3", rule = "deleted-schedule", completed = true, tombstone = true)

    assertThat(counter().count()).isEqualTo(mapOf(PayeeId("a") to 2))
  }

  private fun rule(
    conditions: List<Condition> = emptyList(),
    actions: List<RuleAction> = emptyList(),
  ) = TransactionRule(id = RuleId("rule"), conditions = conditions, actions = actions)

  private fun BudgetDatabase.counter() = PayeeRuleCounter(RulesDao(this), RuleContextDao(this))

  private suspend fun BudgetDatabase.insertRule(
    id: String,
    conditions: List<Condition>,
    actions: List<RuleAction> = emptyList(),
    tombstone: Boolean = false,
  ) =
    RulesDao(this)
      .insert(
        Rules(
          id = RuleId(id),
          stage = null,
          conditions = conditions,
          actions = actions,
          tombstone = tombstone,
          conditions_op = And,
        ),
      )

  private suspend fun BudgetDatabase.insertSchedule(
    id: String,
    rule: String,
    completed: Boolean,
    tombstone: Boolean = false,
  ) = schedulesQueries.withoutResult {
    insert(
      id = ScheduleId(id),
      rule = RuleId(rule),
      active = true,
      completed = completed,
      posts_transaction = false,
      tombstone = tombstone,
      name = id,
    )
  }
}
