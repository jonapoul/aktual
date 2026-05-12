package aktual.budget.list.vm

import aktual.api.model.account.FailureReason
import aktual.api.model.sync.UserFile
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface FetchBudgetsResult {
  data class Success(val userFiles: ImmutableList<UserFile>) : FetchBudgetsResult

  sealed interface Failure : FetchBudgetsResult {
    val reason: String?
  }

  data class FailureResponse(val failureReason: FailureReason) : Failure {
    override val reason: String
      get() = failureReason.reason
  }

  data class InvalidResponse(override val reason: String) : Failure

  data class NetworkFailure(override val reason: String) : Failure

  data class OtherFailure(override val reason: String) : Failure
}
