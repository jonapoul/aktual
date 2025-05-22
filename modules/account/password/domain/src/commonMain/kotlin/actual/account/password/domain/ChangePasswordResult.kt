package actual.account.password.domain

import dev.drewhamilton.poko.Poko

sealed interface ChangePasswordResult {
  data object Success : ChangePasswordResult

  sealed interface Failure : ChangePasswordResult
  data object InvalidPassword : Failure
  data object NotLoggedIn : Failure
  data object NetworkFailure : Failure
  @Poko class HttpFailure(val code: Int, val reason: String) : Failure
  @Poko class OtherFailure(val reason: String) : Failure
}
