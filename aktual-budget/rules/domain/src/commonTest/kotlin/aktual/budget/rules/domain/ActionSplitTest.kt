package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.serialization.json.JsonPrimitive

class ActionSplitTest {
  private val payee = set(Payee, "tesco")
  private val amount1 = splitAmount(1, FixedAmount, 2000)
  private val category1 = set(Category, "household", splitIndex = 1)
  private val amount2 = splitAmount(2, Remainder)
  private val category2 = set(Category, "groceries", splitIndex = 2)
  private val actions = listOf(payee, amount1, category1, amount2, category2)

  @Test
  fun `Actions group by split with their positions`() {
    val splits = actions.splits()
    assertThat(splits.map { it.index }).containsExactly(0, 1, 2)
    assertThat(splits[0]).all {
      prop(ActionSplit::amount).isNull()
      prop(ActionSplit::actions).containsExactly(IndexedValue(0, payee))
    }
    assertThat(splits[1]).all {
      prop(ActionSplit::amount).isEqualTo(IndexedValue(1, amount1))
      prop(ActionSplit::actions).containsExactly(IndexedValue(2, category1))
    }
    assertThat(splits[2]).all {
      prop(ActionSplit::amount).isEqualTo(IndexedValue(3, amount2))
      prop(ActionSplit::actions).containsExactly(IndexedValue(4, category2))
    }
  }

  @Test
  fun `The whole transaction is always a split`() {
    val splits = emptyList<RuleAction>().splits()
    assertThat(splits.map { it.index }).containsExactly(0)
    assertThat(splits[0].actions).isEqualTo(emptyList<IndexedValue<RuleAction>>())
  }

  @Test
  fun `Adding an action keeps the list grouped by split`() {
    val added = actions.addAction(1)
    assertThat(added[3]).isEqualTo(newAction(Payee, splitIndex = 1))
    assertThat(added.splits()[1].actions.map { it.value.field })
      .isEqualTo(listOf<Field?>(Category, Payee))
    assertThat(listOf(payee).addAction(0).last().field).isEqualTo(Category)
  }

  @Test
  fun `A new action goes after its split's last action`() {
    assertThat(actions.newActionIndex(0)).isEqualTo(1)
    assertThat(actions.newActionIndex(1)).isEqualTo(3)
    assertThat(actions.newActionIndex(2)).isEqualTo(5)
    assertThat(emptyList<RuleAction>().newActionIndex(0)).isEqualTo(0)
  }

  @Test
  fun `A new split takes the remainder and sets a category`() {
    val added = actions.addSplit()
    val split = added.splits()[3]
    assertThat(split.amount?.value?.options)
      .isEqualTo(RuleAction.Options(method = Remainder, splitIndex = 3))
    assertThat(split.actions.map { it.value }).containsExactly(newAction(Category, splitIndex = 3))
  }

  @Test
  fun `The first split is number one`() {
    val split = listOf(payee).addSplit().splits()[1]
    assertThat(split.amount?.value?.splitIndex).isEqualTo(1)
  }

  @Test
  fun `Removing a split moves later ones down`() {
    val removed = actions.removeSplit(1)
    assertThat(removed)
      .containsExactly(
        payee,
        splitAmount(1, Remainder),
        set(Category, "groceries", splitIndex = 1),
      )
  }

  @Test
  fun `The whole transaction can't be removed`() {
    assertThat(actions.removeSplit(0)).isEqualTo(actions)
  }

  @Test
  fun `Changing the method resets the value`() {
    val percent = amount1.withSplitMethod(FixedPercent)
    assertThat(percent.options?.method).isEqualTo(FixedPercent)
    assertThat(percent.value).isEqualTo(JsonPrimitive(0))
    assertThat(percent.splitIndex).isEqualTo(1)
    assertThat(amount1.withSplitMethod(Remainder).value).isNull()
    assertThat(amount1.withSplitMethod(FixedAmount)).isEqualTo(amount1)
  }

  @Test
  fun `Formula split amounts aren't editable`() {
    assertThat(amount1.isEditableSplitAmount).isTrue()
    assertThat(amount2.isEditableSplitAmount).isTrue()
    assertThat(splitAmount(1, Formula, "=1").isEditableSplitAmount).isFalse()
    assertThat(payee.isEditableSplitAmount).isFalse()
  }
}
