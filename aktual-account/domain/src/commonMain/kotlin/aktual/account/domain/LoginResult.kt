package aktual.account.domain

import aktual.core.model.Token
import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoginResult {
  data class Success(
    val token: Token,
  ) : LoginResult

  @Immutable
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
