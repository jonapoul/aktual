package dev.jonpoulton.actual.listbudgets.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ListBudgetsState {
  @Immutable
  data class Success(val budgets: ImmutableList<Budget>) : ListBudgetsState

  @Immutable
  data class Failure(val reason: String) : ListBudgetsState

  @Immutable
  data object Loading : ListBudgetsState
}
