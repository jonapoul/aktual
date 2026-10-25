package aktual.budget.rules.vm.search

import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchRulesState {
  data object NoQuery : SearchRulesState

  data object Loading : SearchRulesState

  data object NoResults : SearchRulesState

  @JvmInline value class Failure(val cause: String?) : SearchRulesState

  data class Results(val query: String, val rules: ImmutableList<Rule>) : SearchRulesState
}
