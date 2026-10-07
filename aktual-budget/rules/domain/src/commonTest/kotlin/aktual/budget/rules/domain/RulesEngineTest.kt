package aktual.budget.rules.domain

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

// Ported from runRules tests in
// packages/loot-core/src/server/transactions/transaction-rules.test.ts
class RulesEngineTest {
  @Test
  fun `Runs all the rules in each phase`() {
    val engine =
      RulesEngine(
        listOf(
          rule(
            id = "a",
            conditions =
              listOf(
                cond(Payee, OneOf, listOf("kroger", "kroger1", "kroger2", "kroger3", "kroger4")),
              ),
            actions = listOf(set(Notes, "got it2")),
            stage = Post,
          ),
          rule(
            id = "b",
            conditions = listOf(cond(ImportedPayee, Is, "123 kroger")),
            actions = listOf(set(Payee, "kroger3")),
            stage = Pre,
          ),
          rule(
            id = "c",
            conditions = listOf(cond(ImportedPayee, Contains, "kroger")),
            actions = listOf(set(Payee, "kroger4")),
          ),
          rule(
            id = "d",
            conditions = listOf(cond(Payee, Is, "kroger4")),
            actions = listOf(set(Notes, "got it")),
          ),
        ),
      )

    val result =
      engine.run(tx(importedPayee = "123 kroger", date = "2020-08-11", amount = 50), context())
    assertThat(result)
      .isEqualTo(
        tx(
          importedPayee = "123 kroger",
          date = "2020-08-11",
          amount = 50,
          payee = "kroger4",
          notes = "got it2",
        ),
      )
  }

  @Test
  fun `Payee is nothing matches a transaction without a payee`() {
    var engine =
      RulesEngine(listOf(rule("a", listOf(cond(Payee, Is, null)), listOf(set(Notes, "no payee")))))
    assertThat(engine.run(tx(importedPayee = "kroger"), context()).notes).isEqualTo("no payee")

    // A payee set by a rule action is kept
    engine =
      RulesEngine(
        listOf(
          rule("a", listOf(cond(Payee, Is, null)), listOf(set(Notes, "no payee"))),
          rule("b", listOf(cond(Payee, Is, null)), listOf(set(Payee, "kroger"))),
        ),
      )
    val result = engine.run(tx(importedPayee = "kroger"), context())
    assertThat(result.payee).isEqualTo(PayeeId("kroger"))
    assertThat(result.notes).isEqualTo("no payee")
  }

  @Test
  fun `Category is nothing matches a transaction without a category`() {
    val engine =
      RulesEngine(listOf(rule("a", listOf(cond(Category, Is, null)), listOf(set(Notes, "none")))))
    assertThat(engine.run(tx(payee = "kroger"), context()).notes).isEqualTo("none")
  }

  @Test
  fun `category_group matches categories in that group`() {
    val engine =
      RulesEngine(
        listOf(
          rule("a", listOf(cond(CategoryGroup, Is, "bills")), listOf(set(Notes, "bills-matched"))),
        ),
      )
    val context =
      context(
        categoryGroups =
          mapOf(
            CategoryId("electric") to CategoryGroupId("bills"),
            CategoryId("movies") to CategoryGroupId("fun"),
          ),
      )
    assertThat(engine.run(tx(category = "electric", notes = ""), context).notes)
      .isEqualTo("bills-matched")
    assertThat(engine.run(tx(category = "movies", notes = ""), context).notes).isEqualTo("")
  }

  @Test
  fun `category_group sees a category set earlier in the same run`() {
    val engine =
      RulesEngine(
        listOf(
          rule(
            "a",
            listOf(cond(Payee, Is, "power_co")),
            listOf(set(Category, "electric")),
            stage = Pre,
          ),
          rule(
            "b",
            listOf(cond(CategoryGroup, Is, "bills")),
            listOf(set(Notes, "bills-matched")),
            stage = Post,
          ),
        ),
      )
    val context =
      context(categoryGroups = mapOf(CategoryId("electric") to CategoryGroupId("bills")))
    val result = engine.run(tx(payee = "power_co", notes = ""), context)
    assertThat(result.category).isEqualTo(CategoryId("electric"))
    assertThat(result.notes).isEqualTo("bills-matched")
  }

  @Test
  fun `Payee rules match after an earlier rule sets the payee name`() {
    val engine =
      RulesEngine(
        listOf(
          rule(
            "a",
            listOf(cond(ImportedPayee, Is, "AMZN MKTP")),
            listOf(set(PayeeName, "Amazon")),
            stage = Pre,
          ),
          rule("b", listOf(cond(Payee, Is, "amazon_id")), listOf(set(Category, "shopping"))),
        ),
      )
    val context = context(payees = listOf(RulePayee(PayeeId("amazon_id"), "Amazon")))
    val result = engine.run(tx(importedPayee = "AMZN MKTP"), context)
    assertThat(result.payee).isEqualTo(PayeeId("amazon_id"))
    assertThat(result.category).isEqualTo(CategoryId("shopping"))
  }

  @Test
  fun `payee_name conditions see a name set by an earlier rule`() {
    val engine =
      RulesEngine(
        listOf(
          rule(
            "a",
            listOf(cond(ImportedPayee, Is, "AMZN")),
            listOf(set(PayeeName, "Amazon")),
            stage = Pre,
          ),
          rule("b", listOf(cond(PayeeName, Is, "amazon")), listOf(set(Notes, "renamed"))),
        ),
      )
    assertThat(engine.run(tx(importedPayee = "AMZN"), context()).notes).isEqualTo("renamed")
  }

  @Test
  fun `More specific rules win within a stage`() {
    val engine =
      RulesEngine(
        listOf(
          rule(
            "general",
            listOf(cond(ImportedPayee, Contains, "shop")),
            listOf(set(Category, "general")),
          ),
          rule(
            "specific",
            listOf(cond(ImportedPayee, Is, "big shop")),
            listOf(set(Category, "specific")),
          ),
        ),
      )
    assertThat(engine.run(tx(importedPayee = "Big Shop"), context()).category)
      .isEqualTo(CategoryId("specific"))
    assertThat(engine.run(tx(importedPayee = "Small shop"), context()).category)
      .isEqualTo(CategoryId("general"))
  }

  @Test
  fun `Ids in rules follow merged payees and categories`() {
    val engine =
      RulesEngine(
        rules =
          listOf(
            rule("a", listOf(cond(Payee, Is, "old-payee")), listOf(set(Category, "old-category"))),
            rule(
              "b",
              listOf(cond(Payee, OneOf, listOf("old-payee", "other"))),
              listOf(set(Notes, "oneOf")),
            ),
          ),
        idMappings = mapOf("old-payee" to "new-payee", "old-category" to "new-category"),
      )
    val result = engine.run(tx(payee = "new-payee"), context())
    assertThat(result.category).isEqualTo(CategoryId("new-category"))
    assertThat(result.notes).isEqualTo("oneOf")
  }

  @Test
  fun `A schedule's transaction runs its rule regardless of conditions and skips other schedules`() {
    val rules =
      listOf(
        rule(
          "schedule-rule",
          listOf(cond(Amount, Is, 999)),
          listOf(action(LinkSchedule, value = "schedule-1"), set(Category, "rent")),
        ),
        rule(
          "other-schedule-rule",
          listOf(cond(Amount, Is, 100)),
          listOf(action(LinkSchedule, value = "schedule-2"), set(Notes, "other")),
        ),
        rule("plain", listOf(cond(Amount, Is, 100)), listOf(set(Cleared, false))),
      )
    val engine =
      RulesEngine(
        rules,
        scheduleRules =
          mapOf(
            ScheduleId("schedule-1") to RuleId("schedule-rule"),
            ScheduleId("schedule-2") to RuleId("other-schedule-rule"),
          ),
      )

    val fromSchedule = tx(amount = 100).copy(schedule = ScheduleId("schedule-1"))
    val result = engine.run(fromSchedule, context())
    assertThat(result.category).isEqualTo(CategoryId("rent"))
    assertThat(result.notes).isNull()
    assertThat(result.cleared).isEqualTo(false)

    // Without a schedule, every rule runs on its conditions
    val plain = engine.run(tx(amount = 100), context())
    assertThat(plain.schedule).isEqualTo(ScheduleId("schedule-2"))
    assertThat(plain.notes).isEqualTo("other")
    assertThat(plain.category).isNull()
  }

  @Test
  fun `Rules indexed away from the transaction don't run`() {
    // Upstream's indexes only return this rule for transactions whose payee is "a" or whose
    // imported payee starts with "x", so the isNot conditions alone aren't enough
    val engine =
      RulesEngine(
        listOf(
          rule(
            "r",
            listOf(cond(Payee, IsNot, "a"), cond(ImportedPayee, IsNot, "xyz")),
            listOf(set(Notes, "ran")),
          ),
        ),
      )
    assertThat(engine.run(tx(payee = "b", importedPayee = "other"), context()).notes).isNull()
    assertThat(engine.run(tx(payee = "b", importedPayee = "xenon"), context()).notes)
      .isEqualTo("ran")
  }

  @Test
  fun `Split rules run alongside other rules`() {
    val engine =
      RulesEngine(
        listOf(
          rule(
            "split",
            listOf(cond(ImportedPayee, Is, "shop")),
            listOf(splitAmount(1, FixedPercent, 50), splitAmount(2, Remainder)),
          ),
          rule(
            "post",
            listOf(cond(ImportedPayee, Is, "shop")),
            listOf(set(Notes, "split")),
            stage = Post,
          ),
        ),
      )
    val result = engine.run(tx(importedPayee = "shop", amount = -1001, payee = "p"), context())
    assertThat(result.notes).isEqualTo("split")
    assertThat(result.payee).isNull()
    assertThat(result.subtransactions.map { it.amount.toLong() }).isEqualTo(listOf(-500L, -501L))
    assertThat(result.subtransactions.map { it.payee })
      .isEqualTo(listOf(PayeeId("p"), PayeeId("p")))
  }
}
