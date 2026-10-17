package aktual.budget.reports.vm

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.ConditionType
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class QueryConditionsTest {
  @Test
  fun `New conditions start empty with the first operator`() {
    assertThat(newQueryCondition(Payee)).isEqualTo(Condition(Payee, Is, JsonNull, type = Id))
    assertThat(newQueryCondition(Amount))
      .isEqualTo(Condition(Amount, Is, JsonPrimitive(0), type = ConditionType.Number))
    assertThat(newQueryCondition(Cleared))
      .isEqualTo(Condition(Cleared, Is, JsonPrimitive(true), type = ConditionType.Boolean))
  }

  @Test
  fun `Value kinds follow the operator`() {
    assertThat(conditionValueKind(Category, Is)).isEqualTo(Entity)
    assertThat(conditionValueKind(Category, OneOf)).isEqualTo(EntityList)
    assertThat(conditionValueKind(Category, Contains)).isEqualTo(Text)
    assertThat(conditionValueKind(Account, OnBudget)).isEqualTo(NoValue)
    assertThat(conditionValueKind(Notes, Is)).isEqualTo(Text)
    assertThat(conditionValueKind(Date, LessThan)).isEqualTo(Day)
  }

  @Test
  fun `Changing operator keeps a value of the same kind`() {
    val condition = Condition(Payee, Is, JsonPrimitive("payee-1"))
    assertThat(condition.withOperator(IsNot).value).isEqualTo(JsonPrimitive("payee-1"))
    assertThat(condition.withOperator(OneOf).value).isEqualTo(JsonArray(emptyList()))
    assertThat(condition.withOperator(Contains).value).isEqualTo(JsonPrimitive(""))
  }

  @Test
  fun `Conditions the editor can't show aren't editable`() {
    val between = JsonObject(mapOf("num1" to JsonPrimitive(1), "num2" to JsonPrimitive(2)))
    assertThat(Condition(Amount, IsBetween, between).editableKind()).isNull()
    assertThat(Condition(Amount, Is, JsonPrimitive("12")).editableKind()).isNull()
    assertThat(
        Condition(Amount, Is, JsonPrimitive(1), options = ConditionOptions(inflow = true))
          .editableKind(),
      )
      .isNull()
    assertThat(Condition(Date, Is, JsonPrimitive("2024-01")).editableKind()).isNull()
    assertThat(Condition(Acct, Is, JsonNull).editableKind()).isNull()
    assertThat(Condition(Notes, HasTags, JsonPrimitive("#a")).editableKind()).isNull()
  }

  @Test
  fun `Editable conditions`() {
    assertThat(Condition(Date, Is, JsonPrimitive("2024-01-05")).editableKind()).isEqualTo(Day)
    assertThat(Condition(Date, Is, JsonPrimitive("")).editableKind()).isEqualTo(Day)
    assertThat(Condition(Amount, GreaterThan, JsonPrimitive(-500)).editableKind()).isEqualTo(Money)
    assertThat(Condition(Payee, NotOneOf, JsonArray(listOf(JsonPrimitive("a")))).editableKind())
      .isEqualTo(EntityList)
  }

  @Test
  fun `Renaming keeps the order`() {
    val query = FormulaQuery(emptyList(), conditionsOp = null, timeFrame = null)
    val queries = persistentMapOf("a" to query, "b" to query, "c" to query)
    assertThat(queries.renamed("b", "z").keys.toList()).containsExactly("a", "z", "c")
  }
}
