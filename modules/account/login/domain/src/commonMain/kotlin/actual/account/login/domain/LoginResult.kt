package actual.account.login.domain

import actual.account.model.LoginToken
import actual.api.model.account.FailureReason
import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoginResult {
  data class Success(
    val token: LoginToken,
  ) : LoginResult

  sealed interface Failure : LoginResult

  data object InvalidPassword : Failure

  data class HttpFailure(
    val code: Int,
    val message: String,
  ) : Failure

  data class NetworkFailure(
    val reason: FailureReason,
  ) : Failure

  data class OtherFailure(
    val reason: FailureReason,
  ) : Failure
}
