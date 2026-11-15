package aktual.budget.rules.ui.edit

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface EditRuleAction

internal data object NavBack : EditRuleAction

@JvmInline internal value class OpenSchedule(val id: ScheduleId) : EditRuleAction

internal data object Delete : EditRuleAction

internal data object Save : EditRuleAction

@JvmInline internal value class SetStage(val value: RuleStage) : EditRuleAction

@JvmInline internal value class SetConditionsOp(val value: ConditionOp) : EditRuleAction

internal data class SetCondition(val key: Long, val condition: Condition) : EditRuleAction

@JvmInline internal value class AddCondition(val condition: Condition) : EditRuleAction

@JvmInline internal value class RemoveCondition(val key: Long) : EditRuleAction

internal data class SetAction(val key: Long, val action: RuleAction) : EditRuleAction

@JvmInline internal value class AddAction(val action: RuleAction) : EditRuleAction

@JvmInline internal value class RemoveAction(val key: Long) : EditRuleAction

internal data object AddSplit : EditRuleAction

@JvmInline internal value class RemoveSplit(val splitIndex: Int) : EditRuleAction

internal data object ApplyActions : EditRuleAction

@JvmInline internal value class ToggleMatch(val id: TransactionId) : EditRuleAction

internal data object ToggleAllMatches : EditRuleAction
