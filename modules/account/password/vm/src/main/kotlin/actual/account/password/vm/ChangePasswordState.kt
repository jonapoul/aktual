package actual.account.password.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ChangePasswordState {
  data object Loading : ChangePasswordState

  data object Success : ChangePasswordState

  sealed interface Failure : ChangePasswordState
  data object InvalidPassword : Failure
  data object NotLoggedIn : Failure
  data object PasswordsDontMatch : Failure
  data object NetworkFailure : Failure
  data object OtherFailure : Failure
}
