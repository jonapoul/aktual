package aktual.budget.rules.vm.edit

import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.hasUnknownValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface EditRuleState {
  data object Loading : EditRuleState

  data class Success(
    val rule: Rule,
    val isWorking: Boolean,
    // One per condition and action, for animating them in and out
    val conditionKeys: ImmutableList<Long> = persistentListOf(),
    val actionKeys: ImmutableList<Long> = persistentListOf(),
  ) : EditRuleState {
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
  draft: Draft?,
): EditRuleState =
  when {
    loading -> Loading
    failure != null -> failure
    draft == null -> NotFound
    else -> EditRuleState.Success(draft.rule, working, draft.conditionKeys, draft.actionKeys)
  }
