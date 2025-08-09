package actual.budget.sync.vm

import actual.api.model.account.FailureReason
import actual.core.model.Base64String

interface FetchKeyResult {
  data class Success(val key: Base64String) : FetchKeyResult

  sealed interface Failure : FetchKeyResult
  data object NotLoggedIn : Failure
  data class IOFailure(val message: String) : Failure
  data class ResponseFailure(val reason: FailureReason) : Failure
  data object TestFailure : Failure
}
