package actual.budget.list.vm

import actual.budget.model.Budget
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface FetchBudgetsResult {
  @Poko class Success(
    val budgets: List<Budget>,
  ) : FetchBudgetsResult

  sealed interface Failure : FetchBudgetsResult {
    val reason: String?
  }

  data object NotLoggedIn : Failure {
    override val reason = null
  }

  @Poko class FailureResponse(override val reason: String?) : Failure
  @Poko class InvalidResponse(override val reason: String) : Failure
  @Poko class NetworkFailure(override val reason: String) : Failure
  @Poko class OtherFailure(override val reason: String) : Failure
}
