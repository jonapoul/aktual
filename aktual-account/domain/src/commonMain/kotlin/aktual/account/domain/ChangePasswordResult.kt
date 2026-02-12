package aktual.account.domain

sealed interface ChangePasswordResult {
  data object Success : ChangePasswordResult

  sealed interface Failure : ChangePasswordResult

  data object InvalidPassword : Failure

  data object NotLoggedIn : Failure

  data object NetworkFailure : Failure

  data class HttpFailure(val code: Int, val reason: String) : Failure

  data class OtherFailure(val reason: String) : Failure
}
