package aktual.budget.rules.vm.list

import aktual.budget.model.RuleId
import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet

@Immutable
sealed interface ListRulesState {
  data object Loading : ListRulesState

  @JvmInline value class Failure(val cause: String?) : ListRulesState

  data object Empty : ListRulesState

  @JvmInline value class Success(val rules: ImmutableList<Rule>) : ListRulesState
}

@Immutable
sealed interface CheckboxesState {
  data object Inactive : CheckboxesState

  data class Active(val ids: ImmutableSet<RuleId>) : CheckboxesState

  operator fun plus(id: RuleId): CheckboxesState =
    when (this) {
      Inactive -> this
      is Active -> Active((ids + id).toImmutableSet())
    }

  operator fun minus(id: RuleId): CheckboxesState =
    when (this) {
      Inactive -> this
      is Active -> Active((ids - id).toImmutableSet())
    }

  operator fun contains(id: RuleId): Boolean =
    when (this) {
      Inactive -> false
      is Active -> id in ids
    }
}
