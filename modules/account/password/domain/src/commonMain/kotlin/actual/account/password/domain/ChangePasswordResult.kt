package actual.account.password.domain

sealed interface ChangePasswordResult {
  data object Success : ChangePasswordResult

  sealed interface Failure : ChangePasswordResult
  data object InvalidPassword : Failure
  data object NotLoggedIn : Failure
  data object NetworkFailure : Failure
  data class OtherFailure(val reason: String) : Failure
}
