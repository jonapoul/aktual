package aktual.budget.rules.vm

import aktual.budget.model.Condition
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonNull

class RuleTest {
  @Test
  fun `Known values`() {
    assertThat(RULE.hasUnknownValues).isFalse()
  }

  @Test
  fun `Unknown stage`() {
    assertThat(RULE.copy(stage = Unknown).hasUnknownValues).isTrue()
  }

  @Test
  fun `Unknown conditions op`() {
    assertThat(RULE.copy(conditionsOp = Unknown).hasUnknownValues).isTrue()
  }

  @Test
  fun `Unknown condition field`() {
    val rule = RULE.copy(conditions = persistentListOf(CONDITION.copy(field = Unknown)))
    assertThat(rule.hasUnknownValues).isTrue()
  }

  @Test
  fun `Unknown action op`() {
    val rule = RULE.copy(actions = persistentListOf(ACTION.copy(op = Unknown)))
    assertThat(rule.hasUnknownValues).isTrue()
  }

  @Test
  fun `Unknown action method`() {
    val options = RuleAction.Options(method = Unknown)
    val rule = RULE.copy(actions = persistentListOf(ACTION.copy(options = options)))
    assertThat(rule.hasUnknownValues).isTrue()
  }

  private companion object {
    val CONDITION = Condition(field = Payee, operator = Is, value = JsonNull)

    val ACTION = RuleAction(value = null, op = RuleAction.Op.Set, field = Payee)

    val RULE =
      Rule(
        id = RuleId("abc-123"),
        stage = Pre,
        conditions = persistentListOf(CONDITION),
        conditionsOp = And,
        actions = persistentListOf(ACTION),
      )
  }
}
