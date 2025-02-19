package actual.account.login.domain

import actual.account.model.LoginToken
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
    val reason: String,
  ) : Failure

  data class OtherFailure(
    val reason: String,
  ) : Failure
}
