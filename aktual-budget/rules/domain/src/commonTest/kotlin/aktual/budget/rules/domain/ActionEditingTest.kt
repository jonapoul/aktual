package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.serialization.json.JsonPrimitive

class ActionEditingTest {
  @Test
  fun `New actions set a field to an empty value`() {
    assertThat(newAction(Category))
      .isEqualTo(
        RuleAction(value = null, op = RuleAction.Op.Set, field = Category, type = Id),
      )
    assertThat(newAction(Notes, splitIndex = 2))
      .isEqualTo(
        RuleAction(
          value = JsonPrimitive(""),
          op = RuleAction.Op.Set,
          field = Notes,
          type = RuleAction.Type.String,
          options = RuleAction.Options(splitIndex = 2),
        ),
      )
    assertThat(newAction(Amount).value).isEqualTo(JsonPrimitive(0))
    assertThat(newAction(Cleared).value).isEqualTo(JsonPrimitive(true))
  }

  @Test
  fun `Splits can't set the whole transaction's fields`() {
    assertThat(actionFields(0)).isEqualTo(ACTION_FIELDS)
    assertThat(actionFields(1)).isEqualTo(listOf<Field>(Category, Payee, PayeeName, Notes))
  }

  @Test
  fun `New actions take the first field the split doesn't set yet`() {
    val actions = listOf(set(Category, "c"), set(Payee, "p"), set(Category, "c", splitIndex = 1))
    assertThat(actions.nextActionField(0)).isEqualTo(PayeeName)
    assertThat(actions.nextActionField(1)).isEqualTo(Payee)
    assertThat(ACTION_FIELDS.map { set(it, null) }.nextActionField(0)).isEqualTo(Category)
  }

  @Test
  fun `Changing the field resets the value and type`() {
    val action = set(Category, "food").withField(Date)
    assertThat(action.field).isEqualTo(Date)
    assertThat(action.type).isEqualTo(Date)
    assertThat(action.value).isEqualTo(JsonPrimitive(""))
  }

  @Test
  fun `Changing the field drops a template`() {
    val templated =
      action(RuleAction.Op.Set, Notes, "x", RuleAction.Options(template = "{{notes}}"))
    assertThat(templated.withField(PayeeName).options?.template).isNull()
  }

  @Test
  fun `Notes ops keep the field and clear the value`() {
    val action = set(Category, "food", splitIndex = 1).withOp(AppendNotes)
    assertThat(action.op).isEqualTo(AppendNotes)
    assertThat(action.field).isEqualTo(Category)
    assertThat(action.value).isEqualTo(JsonPrimitive(""))
    assertThat(action.splitIndex).isEqualTo(1)
  }

  @Test
  fun `Back to set picks a field the split allows`() {
    val action = action(PrependNotes, Amount, "x", RuleAction.Options(splitIndex = 1))
    val set = action.withOp(RuleAction.Op.Set)
    assertThat(set.field).isEqualTo(Category)
    assertThat(set.value).isNull()
    assertThat(set.splitIndex).isEqualTo(1)
  }

  @Test
  fun `Editable actions`() {
    assertThat(set(Category, "food").editableKind()).isEqualTo(Entity)
    assertThat(set(Category, null).editableKind()).isEqualTo(Entity)
    assertThat(newAction(Category).editableKind()).isEqualTo(Entity)
    assertThat(set(Notes, "x").editableKind()).isEqualTo(Text)
    assertThat(set(Amount, 100).editableKind()).isEqualTo(Money)
    assertThat(set(Date, "2024-01-05").editableKind()).isEqualTo(Day)
    assertThat(set(Cleared, false).editableKind()).isEqualTo(Flag)
    assertThat(action(PrependNotes, value = "x").editableKind()).isEqualTo(Text)
    assertThat(action(DeleteTransaction, value = "").editableKind()).isEqualTo(NoValue)
  }

  @Test
  fun `Actions the editor can't show aren't editable`() {
    val templated = action(RuleAction.Op.Set, Notes, "x", RuleAction.Options(template = "{{a}}"))
    assertThat(templated.editableKind()).isNull()
    val formula = action(RuleAction.Op.Set, Amount, "", RuleAction.Options(formula = "=1"))
    assertThat(formula.editableKind()).isNull()
    assertThat(action(LinkSchedule, value = "schedule").editableKind()).isNull()
    assertThat(splitAmount(1, FixedAmount, 100).editableKind()).isNull()
    assertThat(set(Amount, "12").editableKind()).isNull()
    assertThat(set(Date, "2024-01").editableKind()).isNull()
    assertThat(set(Amount, 100, splitIndex = 1).editableKind()).isNull()
    assertThat(set(Reconciled, true).editableKind()).isNull()
  }
}
