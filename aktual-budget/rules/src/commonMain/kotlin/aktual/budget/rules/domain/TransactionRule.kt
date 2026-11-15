package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage

/** A rule as stored in the `rules` table, before the engine validates and compiles it. */
data class TransactionRule(
  val id: RuleId,
  val conditions: List<Condition>,
  val actions: List<RuleAction>,
  val stage: RuleStage? = null,
  val conditionsOp: ConditionOp = And,
)
