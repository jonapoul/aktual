package actual.login.vm

import actual.core.colorscheme.ColorSchemePreferences
import actual.core.colorscheme.ColorSchemeType
import actual.core.coroutines.ResettableStateFlow
import actual.core.versions.ActualVersions
import actual.core.versions.ActualVersionsStateHolder
import actual.log.Logger
import actual.login.model.LoginToken
import actual.login.model.Password
import actual.login.prefs.LoginPreferences
import actual.url.model.ServerUrl
import actual.url.prefs.ServerUrlPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
  private val logger: Logger,
  private val loginRequester: LoginRequester,
  versionsStateHolder: ActualVersionsStateHolder,
  serverUrlPrefs: ServerUrlPreferences,
  loginPrefs: LoginPreferences,
  colorSchemePreferences: ColorSchemePreferences,
) : ViewModel() {
  private val mutableEnteredPassword = ResettableStateFlow(Password.Empty)
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)

  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()
  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()
  val themeType: StateFlow<ColorSchemeType> = colorSchemePreferences.stateFlow(viewModelScope)

  val serverUrl: StateFlow<ServerUrl> = serverUrlPrefs
    .url
    .asFlow()
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, ServerUrl.Demo)

  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val loginFailure: StateFlow<LoginResult.Failure?> =
    combine(mutableLoginFailure, isLoading) { failure, loading -> if (loading) null else failure }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val token: StateFlow<LoginToken?> = loginPrefs
    .token
    .asFlow()
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  fun clearState() {
    mutableEnteredPassword.reset()
    mutableIsLoading.reset()
    mutableLoginFailure.reset()
  }

  fun onEnterPassword(password: String) {
    mutableEnteredPassword.update { Password(password) }
  }

  fun onClickSignIn() {
    logger.v("onClickSignIn")
    mutableIsLoading.update { true }
    mutableLoginFailure.reset()
    viewModelScope.launch {
      val password = mutableEnteredPassword.value
      val result = loginRequester.logIn(password)

      logger.d("Login result = %s", result)
      mutableIsLoading.update { false }

      if (result is LoginResult.Failure) {
        mutableLoginFailure.update { result }
      }
    }
  }
}
