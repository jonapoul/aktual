package actual.account.password.vm

import actual.account.login.domain.LoginRequester
import actual.account.login.domain.LoginResult
import actual.account.model.LoginToken
import actual.account.model.Password
import actual.account.password.domain.ChangePasswordResult
import actual.account.password.domain.PasswordChanger
import actual.core.versions.ActualVersions
import actual.core.versions.ActualVersionsStateHolder
import alakazam.kotlin.logging.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class ChangePasswordViewModel @Inject internal constructor(
  versionsStateHolder: ActualVersionsStateHolder,
  private val passwordChanger: PasswordChanger,
  private val loginRequester: LoginRequester,
) : ViewModel() {
  private val mutableShowPasswords = MutableStateFlow(false)
  private val mutablePassword1 = MutableStateFlow(Password.Empty)
  private val mutablePassword2 = MutableStateFlow(Password.Empty)
  private val mutableState = MutableStateFlow<ChangePasswordState?>(null)
  private val mutableLoginToken = Channel<LoginToken?>()

  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()

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
    Logger.d("submit %s %s", password, password2)
    if (password2 != password) {
      mutableState.update { ChangePasswordState.PasswordsDontMatch }
      return
    }

    mutableState.update { ChangePasswordState.Loading }
    viewModelScope.launch {
      val changePasswordResult = passwordChanger.submit(password)
      Logger.d("result = %s", changePasswordResult)
      val newState = when (changePasswordResult) {
        ChangePasswordResult.Success -> ChangePasswordState.Success
        ChangePasswordResult.InvalidPassword -> ChangePasswordState.InvalidPassword
        ChangePasswordResult.NetworkFailure -> ChangePasswordState.NetworkFailure
        ChangePasswordResult.NotLoggedIn -> ChangePasswordState.NotLoggedIn
        is ChangePasswordResult.OtherFailure -> ChangePasswordState.OtherFailure
      }
      mutableState.update { newState }

      if (changePasswordResult is ChangePasswordResult.Success) {
        Logger.d("Logging in...")
        val loginResult = loginRequester.logIn(password)
        handleLoginResult(loginResult)
      }
    }
  }

  private suspend fun handleLoginResult(loginResult: LoginResult) {
    Logger.d("loginResult = %s", loginResult)
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
