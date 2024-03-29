package dev.jonpoulton.actual.login.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoginResult {
  @Immutable
  data object Success : LoginResult

  @Immutable
  sealed interface Failure : LoginResult

  @Immutable
  data object InvalidPassword : Failure

  @Immutable
  data class HttpFailure(val code: Int, val message: String) : Failure

  @Immutable
  data class NetworkFailure(val reason: String) : Failure

  @Immutable
  data class OtherFailure(val reason: String) : Failure
}
