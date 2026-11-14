package aktual.budget.rules.ui.edit

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleStage
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface EditRuleAction

internal data object NavBack : EditRuleAction

internal data object Delete : EditRuleAction

internal data object Save : EditRuleAction

@JvmInline internal value class SetStage(val value: RuleStage) : EditRuleAction

@JvmInline internal value class SetConditionsOp(val value: ConditionOp) : EditRuleAction

internal data class SetCondition(val index: Int, val condition: Condition) : EditRuleAction

internal data object AddCondition : EditRuleAction

@JvmInline internal value class RemoveCondition(val index: Int) : EditRuleAction

internal data class SetAction(val index: Int, val action: RuleAction) : EditRuleAction

internal data object AddAction : EditRuleAction

@JvmInline internal value class RemoveAction(val index: Int) : EditRuleAction
