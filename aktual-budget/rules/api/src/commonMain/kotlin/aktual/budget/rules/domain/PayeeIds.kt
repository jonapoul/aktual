package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction.Op.Set
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * The payees this rule's conditions and actions refer to by id, as iterateIds in
 * packages/loot-core/src/server/rules/rule-utils.ts does for the `payee` field.
 */
fun TransactionRule.payeeIds(): Set<PayeeId> = buildSet {
  for (condition in conditions) {
    if (condition.field.isPayee()) addAll(condition.payeeIds())
  }
  for ((value, op, field) in actions) {
    if (op == Set && field?.isPayee() == true) {
      value?.contentOrNull?.let { add(PayeeId(it)) }
    }
  }
}

private fun Condition.payeeIds(): List<PayeeId> =
  when (operator) {
    Is,
    IsNot -> listOfNotNull(value.payeeId())
    OneOf,
    NotOneOf -> (value as? JsonArray).orEmpty().mapNotNull { it.payeeId() }
    Contains,
    DoesNotContain,
    GreaterThan,
    GreaterThanOrEquals,
    HasAnyTag,
    HasTags,
    IsApprox,
    IsBetween,
    LessThan,
    LessThanOrEquals,
    Matches,
    OffBudget,
    OnBudget -> emptyList()
  }

private fun JsonElement.payeeId(): PayeeId? =
  (this as? JsonPrimitive)?.contentOrNull?.let(::PayeeId)

// Description is the payee field's internal name
private fun Field.isPayee(): Boolean = this == Payee || this == Description
