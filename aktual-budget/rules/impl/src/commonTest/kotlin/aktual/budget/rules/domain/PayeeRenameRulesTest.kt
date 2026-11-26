package aktual.budget.rules.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.DbJson
import aktual.budget.db.RuleJson
import aktual.budget.db.Rules
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
import aktual.budget.model.MessageValue
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.test.TestBudgetSync
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

class PayeeRenameRulesTest {
  @Test
  fun `Adds a pre rule renaming the names to the payee`() = runRenameTest {
    // when
    renames.add(listOf("Kroger #1", "Kroger #2"), to = KROGER)

    // then
    assertThat(sync.changes.map { it.row }.distinct()).isEqualTo(listOf("new-rule"))
    assertThat(sync.value("stage")).isEqualTo("pre")
    assertThat(sync.conditions()).isEqualTo(listOf(importedOneOf("Kroger #1", "Kroger #2")))
    assertThat(sync.actions()).isEqualTo(listOf(setPayee(KROGER)))
  }

  @Test
  fun `Adds the names to an existing rename rule for the payee`() = runRenameTest {
    // given
    rule("other-payee", RuleStage.Pre, importedOneOf("Aldi"), setPayee(ALDI))
    rule("not-pre", null, importedOneOf("Kroger"), setPayee(KROGER))
    rule("existing", RuleStage.Pre, importedOneOf("Kroger", "Kroger #1"), setPayee(KROGER))

    // when
    renames.add(listOf("Kroger #1", "Kroger #2"), to = KROGER)

    // then
    assertThat(sync.changes.map { it.row to it.column })
      .isEqualTo(listOf("existing" to "conditions"))
    assertThat(sync.conditions())
      .isEqualTo(listOf(importedOneOf("Kroger", "Kroger #1", "Kroger #2")))
  }

  @Test
  fun `Empty names add nothing`() = runRenameTest {
    // when
    renames.add(listOf(""), to = KROGER)

    // then
    assertThat(sync.changes).isEmpty()
  }

  private class RenameScope(val database: BudgetDatabase) {
    val sync = TestBudgetSync()
    val renames =
      PayeeRenameRulesImpl(
        rulesDao = RulesDao(database),
        budgetSync = sync,
        uuidGenerator = { "new-rule" },
      )

    suspend fun rule(id: String, stage: RuleStage?, condition: Condition, action: RuleAction) =
      RulesDao(database)
        .insert(
          Rules(
            id = RuleId(id),
            stage = stage,
            conditions = listOf(condition),
            actions = listOf(action),
            tombstone = false,
            conditions_op = And,
          ),
        )
  }

  private fun runRenameTest(action: suspend RenameScope.() -> Unit) = runDatabaseTest {
    RenameScope(this).action()
  }

  private companion object {
    val KROGER = PayeeId("kroger")
    val ALDI = PayeeId("aldi")

    fun importedOneOf(vararg names: String) =
      Condition(
        field = ImportedPayee,
        operator = OneOf,
        type = ConditionType.String,
        value = JsonArray(names.map(::JsonPrimitive)),
      )

    fun setPayee(payee: PayeeId) =
      RuleAction(value = JsonPrimitive(payee.value), op = Set, field = Payee, type = Id)

    fun TestBudgetSync.value(column: String): String? =
      (changes.firstOrNull { it.column == column }?.value as? MessageValue.String)?.value

    fun TestBudgetSync.conditions(): List<Condition>? =
      value("conditions")?.let { DbJson.decodeFromString(RuleJson.conditions, it) }

    fun TestBudgetSync.actions(): List<RuleAction>? =
      value("actions")?.let { DbJson.decodeFromString(RuleJson.actions, it) }
  }
}
