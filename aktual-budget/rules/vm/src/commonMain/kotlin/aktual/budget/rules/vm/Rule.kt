package aktual.budget.rules.vm

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class Rule(
  val id: RuleId,
  val stage: RuleStage,
  val conditions: ImmutableList<Condition>,
  val conditionsOp: ConditionOp,
  val actions: ImmutableList<RuleAction>,
)

// Saving would overwrite values from a newer client with "Unknown", so these can't be saved as they
// are
val Rule.hasUnknownValues: Boolean
  get() =
    stage == Unknown ||
      conditionsOp == Unknown ||
      conditions.any { c ->
        c.field == Unknown || c.type == Unknown || c.conditionsOp == Unknown
      } ||
      actions.any { a ->
        a.op == Unknown || a.field == Unknown || a.type == Unknown || a.options?.method == Unknown
      }
