package aktual.budget.list.vm

import aktual.api.model.account.FailureReason
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

  data class FailureResponse(val failureReason: FailureReason) : Failure {
    override val reason: String
      get() = failureReason.reason
  }

  data class InvalidResponse(override val reason: String) : Failure

  data class NetworkFailure(override val reason: String) : Failure

  data class OtherFailure(override val reason: String) : Failure
}
