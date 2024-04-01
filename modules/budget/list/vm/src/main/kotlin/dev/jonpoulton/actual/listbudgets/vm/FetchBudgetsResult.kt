package dev.jonpoulton.actual.listbudgets.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface FetchBudgetsResult {
  @Immutable
  data class Success(val budgets: ImmutableList<BudgetFile>) : FetchBudgetsResult

  @Immutable
  sealed interface Failure : FetchBudgetsResult

  @Immutable
  data class HttpFailure(val code: Int, val message: String) : Failure

  @Immutable
  data class NetworkFailure(val reason: String) : Failure

  @Immutable
  data class OtherFailure(val reason: String) : Failure
}
