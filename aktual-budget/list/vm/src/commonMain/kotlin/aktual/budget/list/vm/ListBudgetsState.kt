package aktual.budget.list.vm

import aktual.budget.model.Budget
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListBudgetsState {
  data class Success(val budgets: ImmutableList<Budget>) : ListBudgetsState

  data class Failure(val reason: String?) : ListBudgetsState

  data object Loading : ListBudgetsState
}
