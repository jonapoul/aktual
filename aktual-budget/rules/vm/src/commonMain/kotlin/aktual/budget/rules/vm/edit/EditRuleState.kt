package aktual.budget.rules.vm.edit

import aktual.budget.model.ScheduleId
import aktual.budget.rules.domain.Rule
import aktual.budget.rules.domain.hasUnknownValues
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
    val hasChanges: Boolean = false,
    // The schedule that owns this rule. Upstream won't delete its rule, or its date condition
    val schedule: ScheduleId? = null,
  ) : EditRuleState {
    val canSave: Boolean
      get() = hasChanges && !rule.hasUnknownValues

    val canDelete: Boolean
      get() = schedule == null
  }

  sealed interface Failure : EditRuleState

  data object NotFound : Failure

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
    else ->
      EditRuleState.Success(
        rule = draft.rule,
        isWorking = working,
        conditionKeys = draft.conditionKeys,
        actionKeys = draft.actionKeys,
        hasChanges = draft.rule != draft.saved,
        schedule = draft.schedule,
      )
  }
