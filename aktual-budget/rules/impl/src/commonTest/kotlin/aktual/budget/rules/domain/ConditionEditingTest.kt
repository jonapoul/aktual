package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.ConditionType
import aktual.budget.model.Operator
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class ConditionEditingTest {
  @Test
  fun `New conditions start empty with the first operator`() {
    assertThat(newCondition(Payee)).isEqualTo(Condition(Payee, Is, JsonNull, type = Id))
    assertThat(newCondition(Amount))
      .isEqualTo(Condition(Amount, Is, JsonPrimitive(0), type = ConditionType.Number))
    assertThat(newCondition(Cleared))
      .isEqualTo(Condition(Cleared, Is, JsonPrimitive(true), type = ConditionType.Boolean))
  }

  @Test
  fun `Value kinds follow the operator`() {
    assertThat(conditionValueKind(Category, Is)).isEqualTo(Entity)
    assertThat(conditionValueKind(Category, OneOf)).isEqualTo(EntityList)
    assertThat(conditionValueKind(Category, Contains)).isEqualTo(Text)
    assertThat(conditionValueKind(Account, OnBudget)).isEqualTo(NoValue)
    assertThat(conditionValueKind(Notes, Is)).isEqualTo(Text)
    assertThat(conditionValueKind(ImportedPayee, OneOf)).isEqualTo(TextList)
    assertThat(conditionValueKind(PayeeName, NotOneOf)).isEqualTo(TextList)
    assertThat(conditionValueKind(Date, LessThan)).isEqualTo(Day)
    assertThat(conditionValueKind(Date, IsBetween)).isEqualTo(DayRange)
  }

  @Test
  fun `Date ranges are editable`() {
    val range = dayRange(JsonPrimitive("2020-01-01"), JsonPrimitive("2020-01-31"))
    assertThat(Condition(Date, IsBetween, range).editableKind()).isEqualTo(DayRange)
    assertThat(Condition(Date, IsBetween, range).editableKind(RuleConditions)).isEqualTo(DayRange)

    val month = dayRange(JsonPrimitive("2020-01"), JsonPrimitive("2020-03"))
    assertThat(Condition(Date, IsBetween, month).editableKind()).isNull()
    assertThat(Condition(Date, IsBetween, JsonPrimitive("2020-01-01")).editableKind()).isNull()
  }

  @Test
  fun `Changing a day to a range uses it for both ends`() {
    val day = JsonPrimitive("2020-01-01")
    assertThat(Condition(Date, Is, day).withOperator(IsBetween).value).isEqualTo(dayRange(day, day))
    assertThat(Condition(Date, Is, JsonPrimitive("")).withOperator(IsBetween).value)
      .isEqualTo(dayRange(JsonPrimitive(""), JsonPrimitive("")))
  }

  @Test
  fun `Changing operator keeps a value of the same kind`() {
    val condition = Condition(Payee, Is, JsonPrimitive("payee-1"))
    assertThat(condition.withOperator(IsNot).value).isEqualTo(JsonPrimitive("payee-1"))
  }

  @Test
  fun `Changing operator carries ids between one and a list`() {
    val one = Condition(Payee, Is, JsonPrimitive("payee-1"))
    assertThat(one.withOperator(OneOf).value).isEqualTo(JsonArray(listOf(JsonPrimitive("payee-1"))))

    val list = Condition(Payee, OneOf, JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))))
    assertThat(list.withOperator(Is).value).isEqualTo(JsonPrimitive("a"))
    assertThat(Condition(Payee, OneOf, JsonArray(emptyList())).withOperator(Is).value)
      .isEqualTo(JsonNull)
  }

  @Test
  fun `Changing operator turns ids into their names as text`() {
    val condition = Condition(Payee, Is, JsonPrimitive("payee-1"))
    assertThat(condition.withOperator(Contains, names = mapOf("payee-1" to "Shop")).value)
      .isEqualTo(JsonPrimitive("Shop"))
    assertThat(condition.withOperator(Contains).value).isEqualTo(JsonPrimitive(""))
  }

  @Test
  fun `Changing operator doesn't turn text into ids`() {
    val condition = Condition(Payee, Contains, JsonPrimitive("Shop"))
    assertThat(condition.withOperator(Is).value).isEqualTo(JsonNull)
    assertThat(condition.withOperator(OneOf).value).isEqualTo(JsonArray(emptyList()))
  }

  @Test
  fun `Text conditions take a list of strings for oneOf`() {
    val condition = Condition(ImportedPayee, Is, JsonPrimitive("abc"))
    assertThat(condition.withOperator(OneOf, RuleConditions).value)
      .isEqualTo(JsonArray(listOf(JsonPrimitive("abc"))))
    assertThat(
        Condition(ImportedPayee, OneOf, JsonArray(listOf(JsonPrimitive("abc"))))
          .withOperator(Contains, RuleConditions)
          .value,
      )
      .isEqualTo(JsonPrimitive("abc"))
    assertThat(
        Condition(ImportedPayee, OneOf, JsonPrimitive("abc")).editableKind(RuleConditions),
      )
      .isNull()
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
    val names = JsonArray(listOf(JsonPrimitive("Card Factory")))
    assertThat(Condition(ImportedPayee, OneOf, names).editableKind(RuleConditions))
      .isEqualTo(TextList)
    assertThat(Condition(Notes, HasTags, JsonPrimitive("#a")).editableKind(RuleConditions))
      .isEqualTo(Text)
  }

  @Test
  fun `Recurring dates are editable with is and isapprox`() {
    val condition = Condition(Date, IsApprox, RECUR, type = Date)
    assertThat(condition.editableKind(RuleConditions)).isEqualTo(Recurrence)
    assertThat(condition.operators(RuleConditions)).isEqualTo(listOf<Operator>(Is, IsApprox))
    assertThat(condition.withOperator(Is, RuleConditions).value).isEqualTo(RECUR)
    assertThat(condition.copy(operator = GreaterThan).editableKind(RuleConditions)).isNull()
  }

  @Test
  fun `Recurring dates round trip through their config`() {
    val config = RECUR.toRecurConfigOrNull()
    assertThat(config?.toJsonElement()?.toRecurConfigOrNull()).isEqualTo(config)
    assertThat(config?.frequency).isEqualTo(Monthly)
  }

  @Test
  fun `Recurring dates with an unknown frequency aren't editable`() {
    val unknown = JsonObject(RECUR + ("frequency" to JsonPrimitive("fortnightly")))
    assertThat(Condition(Date, Is, unknown).editableKind(RuleConditions)).isNull()
  }

  private companion object {
    val RECUR =
      JsonObject(
        mapOf(
          "start" to JsonPrimitive("2025-03-02"),
          "interval" to JsonPrimitive(1),
          "frequency" to JsonPrimitive("monthly"),
          "patterns" to JsonArray(emptyList()),
          "skipWeekend" to JsonPrimitive(false),
          "weekendSolveMode" to JsonPrimitive("after"),
          "endMode" to JsonPrimitive("never"),
          "endOccurrences" to JsonPrimitive(1),
          "endDate" to JsonPrimitive("2025-03-06"),
        ),
      )
  }
}
