package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import kotlin.test.Test
import kotlinx.datetime.LocalDate

// Ported from packages/loot-core/src/server/rules/index.test.ts (describe 'Action' and 'Rule')
class RuleTest {
  @Test
  fun `Set sets a field`() {
    val rule = rule("r", listOf(cond(Notes, Is, "James")), listOf(set(Notes, "Sarah")))
    assertThat(rule.exec(tx(notes = "James")).notes).isEqualTo("Sarah")
    // Doesn't match
    assertThat(rule.exec(tx(notes = "James2")).notes).isEqualTo("James2")
  }

  @Test
  fun `Set sets every kind of field`() {
    val rule =
      rule(
        "r",
        listOf(cond(Notes, Is, "x")),
        listOf(
          set(Category, "groceries"),
          set(Payee, "payee-2"),
          set(Account, "account-2"),
          set(Date, "2021-02-03"),
          set(Field.Amount, -1234),
          set(Cleared, false),
          set(Reconciled, true),
          set(ImportedPayee, "Imported"),
        ),
      )
    assertThat(rule.exec(tx(notes = "x")))
      .isEqualTo(
        RuleTransaction(
          account = AccountId("account-2"),
          date = LocalDate(2021, 2, 3),
          amount = Amount(-1234),
          payee = PayeeId("payee-2"),
          importedPayee = "Imported",
          category = CategoryId("groceries"),
          notes = "x",
          cleared = false,
          reconciled = true,
        )
      )
  }

  @Test
  fun `Setting an id to nothing clears it`() {
    val rule = rule("r", listOf(cond(Notes, Is, "x")), listOf(set(Category, null)))
    assertThat(rule.exec(tx(notes = "x", category = "food")).category).isNull()
  }

  @Test
  fun `Empty account values are invalid`() {
    val engine =
      RulesEngine(listOf(rule("r", listOf(cond(Notes, Is, "x")), listOf(set(Account, "")))))
    assertThat(engine.invalidRules).containsExactly(RuleId("r"))
  }

  @Test
  fun `Unknown action ops and fields are invalid`() {
    assertFailure { CompiledAction.compile(action(Unknown, Notes, "x"), emptyMap()) }
      .messageContains("Invalid action operation")
    assertFailure { CompiledAction.compile(set(Unknown, "x"), emptyMap()) }
      .messageContains("Invalid field for action")
  }

  @Test
  fun `Prepend and append notes`() {
    val prepend =
      rule("r", listOf(cond(Field.Amount, Is, 1)), listOf(action(PrependNotes, value = "[A] ")))
    assertThat(prepend.exec(tx(amount = 1, notes = "Lunch")).notes).isEqualTo("[A] Lunch")
    assertThat(prepend.exec(tx(amount = 1, notes = null)).notes).isEqualTo("[A] ")
    assertThat(prepend.exec(tx(amount = 1, notes = "")).notes).isEqualTo("[A] ")

    val append =
      rule("r", listOf(cond(Field.Amount, Is, 1)), listOf(action(AppendNotes, value = " #food")))
    assertThat(append.exec(tx(amount = 1, notes = "Lunch")).notes).isEqualTo("Lunch #food")
    assertThat(append.exec(tx(amount = 1, notes = null)).notes).isEqualTo(" #food")
    assertThat(append.exec(tx(amount = 1, notes = "Lunch")).tags).isEqualTo(setOf("food"))
  }

  @Test
  fun `Link schedule and delete transaction`() {
    val rule =
      rule(
        "r",
        listOf(cond(Field.Amount, Is, 1)),
        listOf(action(LinkSchedule, value = "schedule-1"), action(DeleteTransaction, value = "")),
      )
    val result = rule.exec(tx(amount = 1))
    assertThat(result.schedule).isEqualTo(ScheduleId("schedule-1"))
    assertThat(result.tombstone).isTrue()
  }

  @Test
  fun `Template and formula actions are skipped`() {
    val rule =
      rule(
        "r",
        listOf(cond(Notes, Is, "x")),
        listOf(
          action(RuleAction.Op.Set, Notes, "", RuleAction.Options(template = "Hey {{notes}}")),
          action(RuleAction.Op.Set, Field.Amount, 0, RuleAction.Options(formula = "=100")),
          set(Category, "food"),
        ),
      )
    val result = rule.exec(tx(notes = "x", amount = 5))
    assertThat(result.notes).isEqualTo("x")
    assertThat(result.amount).isEqualTo(Amount(5))
    assertThat(result.category).isEqualTo(CategoryId("food"))
  }

  @Test
  fun `Setting payee_name renames the payee`() {
    val context = context(payees = listOf(RulePayee(PayeeId("amazon"), "Amazon")))
    val rule =
      rule("r", listOf(cond(ImportedPayee, Contains, "amzn")), listOf(set(PayeeName, "AMAZON")))
    // Looked up case-insensitively
    assertThat(rule.exec(tx(importedPayee = "AMZN Mktp"), context).payee)
      .isEqualTo(PayeeId("amazon"))
  }

  @Test
  fun `Setting payee_name to a new name creates the payee once`() {
    val context = context()
    val rule =
      rule("r", listOf(cond(ImportedPayee, Contains, "tesco")), listOf(set(PayeeName, "Tesco")))
    val first = rule.exec(tx(importedPayee = "TESCO 123"), context)
    val second = rule.exec(tx(importedPayee = "TESCO 456"), context)
    assertThat(first.payee).isEqualTo(PayeeId("new-payee-1"))
    assertThat(second.payee).isEqualTo(PayeeId("new-payee-1"))
    assertThat(context.createdPayees).isEqualTo(mapOf(PayeeId("new-payee-1") to "Tesco"))
  }

  @Test
  fun `Setting payee_name to nothing clears the payee`() {
    val rule = rule("r", listOf(cond(Field.Amount, Is, 1)), listOf(set(PayeeName, "")))
    assertThat(rule.exec(tx(amount = 1, payee = "p")).payee).isNull()
  }

  @Test
  fun `Executing a rule with several actions`() {
    val rule =
      rule(
        "r",
        listOf(cond(Notes, Is, "James")),
        listOf(set(Notes, "Sarah"), set(Category, "Sarah")),
      )
    val result = rule.exec(tx(notes = "James"))
    assertThat(result.notes).isEqualTo("Sarah")
    assertThat(result.category).isEqualTo(CategoryId("Sarah"))
    assertThat(rule.exec(tx(notes = "James2"))).isEqualTo(tx(notes = "James2"))
  }

  @Test
  fun `Rule with a cleared condition matches on cleared status`() {
    val rule = rule("r", listOf(cond(Cleared, Is, true)), listOf(set(Notes, "Sarah")))
    assertThat(rule.exec(tx(cleared = true)).notes).isEqualTo("Sarah")
    assertThat(rule.exec(tx(cleared = false)).notes).isNull()
  }

  @Test
  fun `A rule without conditions never runs`() {
    val rule = rule("r", emptyList(), listOf(set(Notes, "Sarah")))
    assertThat(rule.exec(tx()).notes).isNull()
  }

  @Test
  fun `And rules need every condition`() {
    val rule =
      rule(
        "r",
        listOf(
          cond(Notes, Is, "James"),
          cond(
            Date,
            IsApprox,
            mapOf("start" to "2018-01-12", "frequency" to "monthly", "interval" to 3),
          ),
        ),
        listOf(set(Notes, "Sarah")),
        conditionsOp = And,
      )
    assertThat(rule.exec(tx(notes = "James", date = "2018-01-12")).notes).isEqualTo("Sarah")
    assertThat(rule.exec(tx(notes = "James2", date = "2018-01-12")).notes).isEqualTo("James2")
    assertThat(rule.exec(tx(notes = "James", date = "2018-01-10")).notes).isEqualTo("Sarah")
    assertThat(rule.exec(tx(notes = "James", date = "2018-01-15")).notes).isEqualTo("James")
  }

  @Test
  fun `Or rules need any condition`() {
    val rule =
      rule(
        "r",
        listOf(
          cond(Notes, Is, "James"),
          cond(
            Date,
            IsApprox,
            mapOf("start" to "2018-01-12", "frequency" to "monthly", "interval" to 3),
          ),
        ),
        listOf(set(Notes, "Sarah")),
        conditionsOp = Or,
      )
    assertThat(rule.exec(tx(notes = "James", date = "2018-01-12")).notes).isEqualTo("Sarah")
    assertThat(rule.exec(tx(notes = "James2", date = "2018-01-12")).notes).isEqualTo("Sarah")
    assertThat(rule.exec(tx(notes = "James", date = "2018-01-15")).notes).isEqualTo("Sarah")
    assertThat(rule.exec(tx(notes = "James2", date = "2018-01-15")).notes).isEqualTo("James2")
  }

  @Test
  fun `Unknown conditionsOp is treated as and`() {
    val rule =
      rule(
        "r",
        listOf(cond(Notes, Is, "a"), cond(Field.Amount, Is, 1)),
        listOf(set(Category, "c")),
        conditionsOp = Unknown,
      )
    assertThat(rule.exec(tx(notes = "a", amount = 2)).category).isNull()
    assertThat(rule.exec(tx(notes = "a", amount = 1)).category).isEqualTo(CategoryId("c"))
  }

  @Test
  fun `A transaction's tags come from its notes`() {
    assertThat(tx(notes = "Dinner #Food ##escaped #travel").tags).isEqualTo(setOf("food", "travel"))
    assertThat(tx(notes = null).tags).isEqualTo(emptySet())
  }

  @Test
  fun `Invalid rules are skipped and listed`() {
    val engine =
      RulesEngine(
        listOf(
          rule("bad", listOf(cond(Notes, Contains, "")), listOf(set(Category, "c"))),
          rule("good", listOf(cond(Notes, Contains, "a")), listOf(set(Category, "c"))),
        )
      )
    assertThat(engine.invalidRules).containsExactly(RuleId("bad"))
    assertThat(engine.rankedRules).containsExactly(RuleId("good"))
    assertThat(engine.run(tx(notes = "a"), context()).category).isEqualTo(CategoryId("c"))
  }

  @Test
  fun `Split rules don't apply to existing splits`() {
    val rule =
      rule(
        "r",
        listOf(cond(Field.Amount, Is, 200)),
        listOf(splitAmount(1, FixedAmount, 100), splitAmount(2, FixedAmount, 100)),
      )
    val split = tx(amount = 200).copy(isParent = true, subtransactions = listOf(tx(amount = 200)))
    assertThat(rule.exec(split)).isEqualTo(split)
    val child = tx(amount = 200).copy(isChild = true)
    assertThat(rule.exec(child)).isEqualTo(child)
  }

  @Test
  fun `Rules on a transaction without splits leave it unsplit`() {
    val result =
      rule("r", listOf(cond(Field.Amount, Is, 1)), listOf(set(Notes, "n", splitIndex = 0)))
        .exec(tx(amount = 1))
    assertThat(result.isParent).isFalse()
    assertThat(result.subtransactions).isEqualTo(emptyList())
  }
}
