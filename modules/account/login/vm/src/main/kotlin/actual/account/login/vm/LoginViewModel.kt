package actual.account.login.vm

import actual.account.login.domain.LoginRequester
import actual.account.login.domain.LoginResult
import actual.account.model.LoginToken
import actual.account.model.Password
import actual.core.model.ActualVersions
import actual.core.model.ActualVersionsStateHolder
import actual.core.model.ServerUrl
import actual.prefs.LoginPreferences
import actual.prefs.ServerUrlPreferences
import alakazam.kotlin.core.ResettableStateFlow
import alakazam.kotlin.logging.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.preferences.core.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject internal constructor(
  private val loginRequester: LoginRequester,
  versionsStateHolder: ActualVersionsStateHolder,
  serverUrlPrefs: ServerUrlPreferences,
  loginPrefs: LoginPreferences,
  passwordProvider: Password.Provider,
) : ViewModel() {
  private val mutableEnteredPassword = ResettableStateFlow(passwordProvider.default())
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)

  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()
  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()
  val serverUrl: StateFlow<ServerUrl?> = serverUrlPrefs.url.asStateFlow(viewModelScope)
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val loginFailure: StateFlow<LoginResult.Failure?> =
    combine(mutableLoginFailure, isLoading) { failure, loading -> if (loading) null else failure }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val token: Flow<LoginToken> = loginPrefs
    .token
    .asFlow()
    .filterNotNull()

  fun clearState() {
    mutableEnteredPassword.reset()
    mutableIsLoading.reset()
    mutableLoginFailure.reset()
  }

  fun onEnterPassword(password: String) {
    mutableEnteredPassword.update { Password(password) }
  }

  fun onClickSignIn() {
    Logger.v("onClickSignIn")
    mutableIsLoading.update { true }
    mutableLoginFailure.reset()
    viewModelScope.launch {
      val password = mutableEnteredPassword.value
      val result = loginRequester.logIn(password)

      Logger.d("Login result = %s", result)
      mutableIsLoading.update { false }

      if (result is LoginResult.Failure) {
        mutableLoginFailure.update { result }
      }
    }
  }
}
