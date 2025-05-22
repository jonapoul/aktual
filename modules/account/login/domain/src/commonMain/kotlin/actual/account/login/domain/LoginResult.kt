package actual.account.login.domain

import actual.account.model.LoginToken
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface LoginResult {
  @Poko class Success(
    val token: LoginToken,
  ) : LoginResult

  sealed interface Failure : LoginResult

  data object InvalidPassword : Failure

  @Poko class HttpFailure(
    val code: Int,
    val message: String,
  ) : Failure

  @Poko class NetworkFailure(
    val reason: String,
  ) : Failure

  @Poko class OtherFailure(
    val reason: String,
  ) : Failure
}
