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

@Immutable internal sealed interface ViewModelAction : EditRuleAction

internal data object Delete : ViewModelAction

internal data object Save : ViewModelAction

internal data object Revert : ViewModelAction

@JvmInline internal value class SetStage(val value: RuleStage) : ViewModelAction

@JvmInline internal value class SetConditionsOp(val value: ConditionOp) : ViewModelAction

internal data class SetCondition(val key: Long, val condition: Condition) : ViewModelAction

@JvmInline internal value class AddCondition(val condition: Condition) : ViewModelAction

@JvmInline internal value class RemoveCondition(val key: Long) : ViewModelAction

internal data class SetAction(val key: Long, val action: RuleAction) : ViewModelAction

@JvmInline internal value class AddAction(val action: RuleAction) : ViewModelAction

@JvmInline internal value class RemoveAction(val key: Long) : ViewModelAction

internal data object AddSplit : ViewModelAction

@JvmInline internal value class RemoveSplit(val splitIndex: Int) : ViewModelAction

internal data object ApplyActions : ViewModelAction

@JvmInline internal value class ToggleMatch(val id: TransactionId) : ViewModelAction

internal data object ToggleAllMatches : ViewModelAction
