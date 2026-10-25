package aktual.budget.rules.domain

import aktual.budget.db.dao.RuleContextDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction.Op.Set
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    if (condition.field.toRuleField() == Payee) addAll(condition.payeeIds())
  }
  for ((value, op, field) in actions) {
    if (op == Set && field?.toRuleField() == Payee) {
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

/**
 * Counts the rules referring to each payee, as getPayeeRuleCounts in
 * packages/loot-core/src/server/payees/app.ts. Rules of completed schedules are left out. Unlike
 * upstream, a rule counts once per payee, however many times it mentions it.
 */
@Inject
class PayeeRuleCounter(private val rulesDao: RulesDao, private val contextDao: RuleContextDao) {
  // Emits whenever the counts might have changed
  fun observeChanges(): Flow<Unit> =
    combine(rulesDao.observeChanges(), contextDao.observeCompletedScheduleRuleChanges()) { _, _ -> }

  suspend fun count(): Map<PayeeId, Int> {
    val completed = contextDao.completedScheduleRules()
    val counts = HashMap<PayeeId, Int>()
    for (row in rulesDao.getAll()) {
      if (row.id in completed) continue
      for (id in row.toTransactionRule().payeeIds()) {
        counts[id] = (counts[id] ?: 0) + 1
      }
    }
    return counts
  }
}
