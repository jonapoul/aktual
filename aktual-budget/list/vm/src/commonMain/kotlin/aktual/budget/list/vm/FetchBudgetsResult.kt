package aktual.budget.list.vm

import aktual.budget.model.Budget
import androidx.compose.runtime.Immutable

@Immutable
sealed interface FetchBudgetsResult {
  data class Success(val budgets: List<Budget>) : FetchBudgetsResult

  sealed interface Failure : FetchBudgetsResult {
    val reason: String?
  }

  data object NotLoggedIn : Failure {
    override val reason = null
  }

  data class FailureResponse(override val reason: String?) : Failure

  data class InvalidResponse(override val reason: String) : Failure

  data class NetworkFailure(override val reason: String) : Failure

  data class OtherFailure(override val reason: String) : Failure
}
