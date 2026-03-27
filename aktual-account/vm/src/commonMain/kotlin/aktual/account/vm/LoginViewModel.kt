package aktual.account.vm

import aktual.account.domain.LoginRequester
import aktual.account.domain.LoginResult
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.prefs.AppPreferences
import alakazam.kotlin.ResettableStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@ViewModelKey(LoginViewModel::class)
@ContributesIntoMap(AppScope::class)
class LoginViewModel(
  private val loginRequester: LoginRequester,
  versionsStateHolder: AktualVersionsStateHolder,
  preferences: AppPreferences,
  buildConfig: BuildConfig,
) : ViewModel() {
  private val mutableEnteredPassword = ResettableStateFlow(buildConfig.defaultPassword)
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)
  private val mutableRedirectUrl = ResettableStateFlow<String?>(null)
  private val mutableLoginMethods = MutableStateFlow<ImmutableList<LoginMethod>>(persistentListOf())
  private val mutableSelectedLoginMethod = MutableStateFlow(LoginMethod.Password)

  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()
  val versions: StateFlow<AktualVersions> = versionsStateHolder.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()
  val redirectUrl: StateFlow<String?> = mutableRedirectUrl.asStateFlow()
  val loginMethods: StateFlow<ImmutableList<LoginMethod>> = mutableLoginMethods.asStateFlow()
  val selectedLoginMethod: StateFlow<LoginMethod> = mutableSelectedLoginMethod.asStateFlow()

  val loginFailure: StateFlow<LoginResult.Failure?> =
    combine(mutableLoginFailure, isLoading) { failure, loading -> if (loading) null else failure }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val token: Flow<Token> = preferences.token.asFlow().filterNotNull()

  init {
    viewModelScope.launch {
      val availableLoginMethods = loginRequester.fetchLoginMethods()
      val loginMethods = availableLoginMethods.map { it.method }.toImmutableList()
      mutableLoginMethods.update { loginMethods }
      val firstActive = availableLoginMethods.firstOrNull { it.isActive }
      if (firstActive != null) {
        mutableSelectedLoginMethod.update { firstActive.method }
      }
    }
  }

  fun clearState() {
    mutableEnteredPassword.reset()
    mutableIsLoading.reset()
    mutableLoginFailure.reset()
    mutableRedirectUrl.reset()
  }

  fun onEnterPassword(password: String) {
    mutableEnteredPassword.update { Password(password) }
  }

  fun onSelectLoginMethod(method: LoginMethod) {
    mutableSelectedLoginMethod.update { method }
    mutableLoginFailure.reset()
  }

  fun onClickSignIn(loginMethod: LoginMethod = mutableSelectedLoginMethod.value) {
    logcat.v { "onClickSignIn with method=$loginMethod" }
    val password = mutableEnteredPassword.value
    if (loginMethod == LoginMethod.Password && password == Password.Empty) return
    mutableIsLoading.update { true }
    mutableLoginFailure.reset()
    viewModelScope.launch {
      val result = loginRequester.logIn(password, loginMethod)

      logcat.d { "Login result = $result" }
      mutableIsLoading.update { false }

      when (result) {
        is LoginResult.Failure -> mutableLoginFailure.update { result }
        is LoginResult.Redirect -> mutableRedirectUrl.update { result.url }
        is LoginResult.Success -> Unit // handled via token flow
      }
    }
  }
}
