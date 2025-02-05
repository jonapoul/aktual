package actual.login.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoginResult {
  data object Success : LoginResult

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
