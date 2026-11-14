package aktual.budget.rules.domain

import aktual.budget.model.RuleAction
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonPrimitive

/**
 * A rule's actions for one split, with each action's position in the rule's flat list. [index] 0 is
 * the whole transaction, which has no [amount].
 */
@Immutable
data class ActionSplit(
  val index: Int,
  val amount: IndexedValue<RuleAction>?,
  val actions: ImmutableList<IndexedValue<RuleAction>>,
)

// Split 0 is always there, even with no actions, as upstream's editor shows it
fun List<RuleAction>.splits(): ImmutableList<ActionSplit> {
  val indexed = withIndex().groupBy { it.value.splitIndex }
  val count = (indexed.keys.maxOrNull() ?: 0) + 1
  return List(count) { split ->
      val group = indexed[split].orEmpty()
      ActionSplit(
        index = split,
        amount = group.firstOrNull { it.value.op == SetSplitAmount },
        actions = group.filter { it.value.op != SetSplitAmount }.toImmutableList(),
      )
    }
    .toImmutableList()
}

// Goes after the split's last action, so the list stays grouped by split
fun List<RuleAction>.addAction(splitIndex: Int): List<RuleAction> {
  val action = newAction(nextActionField(splitIndex), splitIndex)
  return toMutableList().apply { add(newActionIndex(splitIndex), action) }
}

// Where [addAction] puts the split's new action
fun List<RuleAction>.newActionIndex(splitIndex: Int): Int {
  val last = indexOfLast { it.splitIndex == splitIndex }
  return if (last < 0) size else last + 1
}

// A new split takes the remainder until its amount is set, as upstream's does
fun List<RuleAction>.addSplit(): List<RuleAction> {
  val index = (maxOfOrNull { it.splitIndex } ?: 0) + 1
  val amount =
    RuleAction(
      op = SetSplitAmount,
      value = null,
      options = RuleAction.Options(method = Remainder, splitIndex = index),
    )
  return this + amount + newAction(nextActionField(index), index)
}

// Later splits move down one to fill the gap
fun List<RuleAction>.removeSplit(splitIndex: Int): List<RuleAction> {
  if (splitIndex == 0) return this
  return filter { it.splitIndex != splitIndex }
    .map { action ->
      val index = action.splitIndex
      if (index > splitIndex) {
        action.copy(options = action.options?.copy(splitIndex = index - 1))
      } else {
        action
      }
    }
}

// The value resets with the method: an amount in cents, a percentage, or nothing for the remainder
fun RuleAction.withSplitMethod(method: RuleAction.Method): RuleAction {
  if (method == options?.method) return this
  val value =
    when (method) {
      FixedAmount,
      FixedPercent -> JsonPrimitive(0)
      Formula,
      Remainder,
      Unknown -> null
    }
  return copy(value = value, options = options?.copy(method = method, formula = null))
}

// False for a formula, which the editor can't show
val RuleAction.isEditableSplitAmount: Boolean
  get() = op == SetSplitAmount && options?.method?.let { it in EDITABLE_METHODS } == true

private val EDITABLE_METHODS = setOf<RuleAction.Method>(FixedAmount, FixedPercent, Remainder)
