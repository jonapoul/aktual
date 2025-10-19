/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.account.vm

import aktual.account.domain.ChangePasswordResult
import aktual.account.domain.LoginRequester
import aktual.account.domain.LoginResult
import aktual.account.domain.PasswordChanger
import aktual.core.di.ViewModelKey
import aktual.core.di.ViewModelScope
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.LoginToken
import aktual.core.model.Password
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat
import kotlin.time.Duration.Companion.seconds

@Inject
@ViewModelKey(ChangePasswordViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class ChangePasswordViewModel internal constructor(
  versionsStateHolder: AktualVersionsStateHolder,
  private val passwordChanger: PasswordChanger,
  private val loginRequester: LoginRequester,
) : ViewModel() {
  private val mutableShowPasswords = MutableStateFlow(false)
  private val mutablePassword1 = MutableStateFlow(Password.Empty)
  private val mutablePassword2 = MutableStateFlow(Password.Empty)
  private val mutableState = MutableStateFlow<ChangePasswordState?>(null)
  private val mutableLoginToken = Channel<LoginToken?>()

  val versions: StateFlow<AktualVersions> = versionsStateHolder.asStateFlow()

  val inputPassword1: StateFlow<Password> = mutablePassword1.asStateFlow()
  val inputPassword2: StateFlow<Password> = mutablePassword2.asStateFlow()
  val showPasswords: StateFlow<Boolean> = mutableShowPasswords.asStateFlow()
  val state: StateFlow<ChangePasswordState?> = mutableState.asStateFlow()
  val loginToken: Flow<LoginToken?> = mutableLoginToken.receiveAsFlow()

  val passwordsMatch: StateFlow<Boolean> = combine(mutablePassword1, mutablePassword2, ::Pair)
    .map { (p1, p2) -> validPasswords(p1, p2) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

  fun setPasswordsVisible(visible: Boolean) = mutableShowPasswords.update { visible }

  fun setPassword1(password: Password) = mutablePassword1.update { password }

  fun setPassword2(password: Password) = mutablePassword2.update { password }

  fun submit() {
    val password = mutablePassword1.value
    val password2 = mutablePassword2.value
    logcat.d { "submit $password $password2" }
    if (password2 != password) {
      mutableState.update { ChangePasswordState.PasswordsDontMatch }
      return
    }

    mutableState.update { ChangePasswordState.Loading }
    viewModelScope.launch {
      val changePasswordResult = passwordChanger.submit(password)
      logcat.d { "result = $changePasswordResult" }
      val newState = when (changePasswordResult) {
        ChangePasswordResult.Success -> ChangePasswordState.Success
        ChangePasswordResult.InvalidPassword -> ChangePasswordState.InvalidPassword
        ChangePasswordResult.NetworkFailure -> ChangePasswordState.NetworkFailure
        ChangePasswordResult.NotLoggedIn -> ChangePasswordState.NotLoggedIn
        is ChangePasswordResult.HttpFailure -> ChangePasswordState.OtherFailure
        is ChangePasswordResult.OtherFailure -> ChangePasswordState.OtherFailure
      }
      mutableState.update { newState }

      if (changePasswordResult is ChangePasswordResult.Success) {
        logcat.d { "Logging in..." }
        val loginResult = loginRequester.logIn(password)
        handleLoginResult(loginResult)
      }
    }
  }

  private suspend fun handleLoginResult(loginResult: LoginResult) {
    logcat.d { "loginResult = $loginResult" }
    if (loginResult is LoginResult.Success) {
      // wait for a second before triggering the navigation, so the user gets the success message
      delay(SUCCESS_DELAY)
      mutableLoginToken.send(loginResult.token)
    }
  }

  private fun validPasswords(p1: Password, p2: Password) = p1.isNotEmpty() && p2.isNotEmpty() && p1 == p2

  private companion object {
    val SUCCESS_DELAY = 1.seconds
  }
}

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
