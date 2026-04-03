package aktual.budget.rules.vm

import aktual.budget.model.Condition
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListRulesState {
  data object Loading : ListRulesState

  @JvmInline value class Failure(val cause: String?) : ListRulesState

  data object Empty : ListRulesState

  @JvmInline value class Success(val rules: ImmutableList<RuleListItem>) : ListRulesState
}

@Immutable
data class RuleListItem(
  val id: RuleId,
  val stage: RuleStage?,
  val conditions: ImmutableList<Condition>,
  val conditionsOp: Condition.Op,
  val actions: ImmutableList<RuleAction>,
)
