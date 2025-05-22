package actual.budget.list.vm

import actual.budget.model.Budget
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListBudgetsState {
  @Poko class Success(
    val budgets: ImmutableList<Budget>,
  ) : ListBudgetsState

  @Poko class Failure(
    val reason: String?,
  ) : ListBudgetsState

  data object Loading : ListBudgetsState
}
