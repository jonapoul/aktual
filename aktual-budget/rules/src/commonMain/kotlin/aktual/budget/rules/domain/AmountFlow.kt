package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

// Upstream's "amount (inflow)" and "amount (outflow)" are amount conditions with these options set.
// They only match money going that way, and compare it as a positive amount
enum class AmountFlow {
  Either,
  Inflow,
  Outflow;

  companion object {
    val all: ImmutableList<AmountFlow> = entries.toImmutableList()
  }
}

val Condition.amountFlow: AmountFlow
  get() =
    when {
      options?.inflow == true -> Inflow
      options?.outflow == true -> Outflow
      else -> Either
    }

// Flowing amounts are positive, so a negative value loses its sign
fun Condition.withAmountFlow(flow: AmountFlow): Condition {
  val options =
    (options ?: ConditionOptions())
      .copy(inflow = true.takeIf { flow == Inflow }, outflow = true.takeIf { flow == Outflow })
      .takeUnless { it == ConditionOptions() }
  val amount = (value as? JsonPrimitive)?.takeUnless { it.isString }?.longOrNull
  val value = if (flow != Either && amount != null) JsonPrimitive(amount.absoluteValue) else value
  return copy(options = options, value = value)
}
