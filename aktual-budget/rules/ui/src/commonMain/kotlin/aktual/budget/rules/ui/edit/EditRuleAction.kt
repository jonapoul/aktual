package aktual.budget.rules.ui.edit

import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.RuleStage
import androidx.compose.runtime.Immutable
import kotlinx.serialization.json.JsonElement

@Immutable internal sealed interface EditRuleAction

internal data object NavBack : EditRuleAction

internal data object Delete : EditRuleAction

internal data object Save : EditRuleAction

internal data object AppendCondition : EditRuleAction

@JvmInline internal value class SetStage(val value: RuleStage) : EditRuleAction

@JvmInline internal value class SetConditionOp(val value: ConditionOp) : EditRuleAction

@JvmInline internal value class DeleteCondition(val index: Int) : EditRuleAction

internal data class SetConditionField(val value: Field, val index: Int) : EditRuleAction

internal data class SetConditionOperator(val value: Operator, val index: Int) : EditRuleAction

internal data class SetConditionValue(val value: JsonElement, val index: Int) : EditRuleAction

@Immutable
internal fun interface EditRuleActionHandler {
  operator fun invoke(action: EditRuleAction)
}
