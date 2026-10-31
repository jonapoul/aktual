package aktual.budget.rules.vm

import aktual.budget.db.Rules
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.Operator
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@Inject
class RulesLoader internal constructor(private val rulesDao: RulesDao) {
  suspend fun load(): ImmutableList<Rule> =
    rulesDao.getAll().map(::toRule).sortedWith(RuleComparator).toImmutableList()

  fun observe(): Flow<ImmutableList<Rule>> = rulesDao.observeChanges().mapLatest { load() }

  private fun toRule(rule: Rules): Rule =
    Rule(
      id = rule.id,
      stage = rule.stage ?: Default,
      conditions = rule.conditions.orEmpty().toImmutableList(),
      conditionsOp = rule.conditions_op ?: And,
      actions = rule.actions.orEmpty().toImmutableList(),
    )

  /**
   * Sorts rules to match execution order. Mirrors rankRules in
   * packages/loot-core/src/server/rules/rule-utils.ts. Groups by stage (pre -> default -> post),
   * then sorts within each stage by condition specificity score, with rule ID as tiebreaker.
   */
  private object RuleComparator : Comparator<Rule> {
    private val EXACT_OPERATORS = setOf<Operator>(Is, IsNot, IsApprox, OneOf, NotOneOf)

    override fun compare(a: Rule, b: Rule): Int {
      val stageDiff = a.stage.ordinal - b.stage.ordinal
      if (stageDiff != 0) return stageDiff
      val scoreDiff = computeScore(b.conditions) - computeScore(a.conditions)
      if (scoreDiff != 0) return scoreDiff
      return a.id.compareTo(b.id)
    }

    private fun computeScore(conditions: List<Condition>): Int {
      val score = conditions.sumOf { it.operator.score() }
      val allExact = conditions.isNotEmpty() && conditions.all { it.operator in EXACT_OPERATORS }
      return if (allExact) score * 2 else score
    }
  }
}
