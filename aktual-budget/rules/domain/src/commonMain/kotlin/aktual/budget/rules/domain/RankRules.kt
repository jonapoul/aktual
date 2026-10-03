package aktual.budget.rules.domain

import aktual.budget.model.Operator
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage

/**
 * Puts [rules] in the order they run: `pre` rules, then those without a stage, then `post` rules.
 * Within a stage, less specific rules run first so that more specific ones get the last word. Port
 * of `rankRules` in packages/loot-core/src/server/rules/rule-utils.ts.
 */
fun rankRules(rules: List<TransactionRule>): List<TransactionRule> =
  rankBy(rules, TransactionRule::id, TransactionRule::stage) { rule ->
    rule.conditions.map { it.operator }
  }

internal fun <T> rankBy(
  rules: List<T>,
  id: (T) -> RuleId,
  stage: (T) -> RuleStage?,
  operators: (T) -> List<Operator>,
): List<T> {
  val scores = rules.associateWith { score(operators(it)) }
  return rules.sortedWith(
    compareBy<T> { stageOrder(stage(it)) }.thenBy { scores.getValue(it) }.thenBy { id(it).value }
  )
}

// Unknown stages run with the unstaged rules, as anything but "pre" or "post" does upstream
private fun stageOrder(stage: RuleStage?): Int =
  when (stage) {
    Pre -> 0
    Post -> 2
    Default,
    Unknown,
    null -> 1
  }

// computeScore in rule-utils.ts
private fun score(operators: List<Operator>): Int {
  val score = operators.sumOf(::operatorScore)
  return if (operators.all { it in EXACT_OPERATORS }) score * 2 else score
}

private val EXACT_OPERATORS = setOf<Operator>(Is, IsNot, IsApprox, OneOf, NotOneOf)

// OP_SCORES in rule-utils.ts
@Suppress("MagicNumber")
private fun operatorScore(operator: Operator): Int =
  when (operator) {
    Is,
    IsNot -> 10
    OneOf,
    NotOneOf -> 9
    IsApprox,
    IsBetween -> 5
    GreaterThan,
    GreaterThanOrEquals,
    LessThan,
    LessThanOrEquals -> 1
    Contains,
    DoesNotContain,
    Matches,
    HasTags,
    HasAnyTag,
    OnBudget,
    OffBudget -> 0
  }
