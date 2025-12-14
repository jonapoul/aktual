package aktual.budget.sync.vm

import aktual.api.model.account.FailureReason
import okio.ByteString

interface FetchKeyResult {
  data class Success(val key: ByteString) : FetchKeyResult

  sealed interface Failure : FetchKeyResult
  data object NotLoggedIn : Failure
  data class IOFailure(val message: String) : Failure
  data class ResponseFailure(val reason: FailureReason) : Failure
  data object TestFailure : Failure
}
