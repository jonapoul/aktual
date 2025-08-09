package actual.account.vm

import actual.account.domain.LoginRequester
import actual.account.domain.LoginResult
import actual.account.model.LoginToken
import actual.account.model.Password
import actual.core.di.ViewModelKey
import actual.core.di.ViewModelScope
import actual.core.model.ActualVersions
import actual.core.model.ActualVersionsStateHolder
import actual.core.model.ServerUrl
import actual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.ResettableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jonpoulton.preferences.core.asStateFlow
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Inject
@ViewModelKey(LoginViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class LoginViewModel(
  private val loginRequester: LoginRequester,
  versionsStateHolder: ActualVersionsStateHolder,
  preferences: AppGlobalPreferences,
  passwordProvider: Password.Provider,
) : ViewModel() {
  private val mutableEnteredPassword = ResettableStateFlow(passwordProvider.default())
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)

  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()
  val versions: StateFlow<ActualVersions> = versionsStateHolder.asStateFlow()
  val serverUrl: StateFlow<ServerUrl?> = preferences.serverUrl.asStateFlow(viewModelScope)
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val loginFailure: StateFlow<LoginResult.Failure?> =
    combine(mutableLoginFailure, isLoading) { failure, loading -> if (loading) null else failure }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val token: Flow<LoginToken> = preferences
    .loginToken
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
    logcat.v { "onClickSignIn" }
    mutableIsLoading.update { true }
    mutableLoginFailure.reset()
    viewModelScope.launch {
      val password = mutableEnteredPassword.value
      val result = loginRequester.logIn(password)

      logcat.d { "Login result = $result" }
      mutableIsLoading.update { false }

      if (result is LoginResult.Failure) {
        mutableLoginFailure.update { result }
      }
    }
  }
}
