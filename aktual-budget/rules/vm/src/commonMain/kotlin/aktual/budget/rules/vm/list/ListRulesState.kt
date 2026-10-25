package aktual.budget.rules.vm.list

import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListRulesState {
  data object Loading : ListRulesState

  @JvmInline value class Failure(val cause: String?) : ListRulesState

  data object Empty : ListRulesState

  @JvmInline value class Success(val rules: ImmutableList<Rule>) : ListRulesState
}
