package aktual.budget.rules.ui.edit

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleStage
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface EditRuleAction

internal data object NavBack : EditRuleAction

internal data object Delete : EditRuleAction

internal data object Save : EditRuleAction

@JvmInline internal value class SetStage(val value: RuleStage) : EditRuleAction

internal data class SetConditions(val conditions: List<Condition>, val op: ConditionOp) :
  EditRuleAction
