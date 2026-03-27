package aktual.budget.list.vm

import aktual.budget.model.Budget
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListBudgetsState {
  @JvmInline value class Success(val budgets: ImmutableList<Budget>) : ListBudgetsState

  @JvmInline value class Failure(val reason: String?) : ListBudgetsState

  @JvmInline value class Loading(val numLoadingItems: Int) : ListBudgetsState
}
