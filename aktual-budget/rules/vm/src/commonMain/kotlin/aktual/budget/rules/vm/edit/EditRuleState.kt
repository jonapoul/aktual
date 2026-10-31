package aktual.budget.rules.vm.edit

import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.hasUnknownValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
sealed interface EditRuleState {
  data object Loading : EditRuleState

  data class Success(val rule: Rule, val isWorking: Boolean) : EditRuleState {
    val canSave: Boolean
      get() = !rule.hasUnknownValues
  }

  sealed interface Failure : EditRuleState

  data object NotFound : Failure

  @JvmInline value class SaveFailed(val reason: String) : Failure

  @JvmInline value class OtherFailure(val reason: String) : Failure
}

@Stable
internal fun editRuleState(
  loading: Boolean,
  working: Boolean,
  failure: EditRuleState.Failure?,
  rule: Rule?,
): EditRuleState =
  when {
    loading -> Loading
    failure != null -> failure
    rule == null -> NotFound
    else -> EditRuleState.Success(rule, working)
  }
