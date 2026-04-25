package aktual.budget.rules.vm.edit

import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
sealed interface EditRuleState {
  data object Loading : EditRuleState

  data class Success(val rule: Rule, val isWorking: Boolean) : EditRuleState

  @Immutable
  sealed interface Failure : EditRuleState {
    data object NoMatch : Failure

    @JvmInline value class Saving(val reason: String) : Failure

    @JvmInline value class Other(val reason: String) : Failure
  }
}

@Stable
internal fun editRuleState(
  loading: Boolean,
  working: Boolean,
  failure: EditRuleState.Failure?,
  rule: Rule?,
): EditRuleState =
  when {
    loading -> EditRuleState.Loading
    failure != null -> failure
    rule == null -> EditRuleState.Failure.NoMatch
    else -> EditRuleState.Success(rule, working)
  }
