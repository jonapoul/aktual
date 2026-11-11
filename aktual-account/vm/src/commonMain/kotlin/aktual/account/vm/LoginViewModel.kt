package aktual.account.vm

import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.di.ServerChosenScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import aktual.prefs.AppPreferences
import alakazam.kotlin.ResettableStateFlow
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(ServerChosenScope::class)
class LoginViewModel(
  private val loginRequester: LoginRequester,
  versionsStateHolder: AktualVersionsStateHolder,
  preferences: AppPreferences,
  buildConfig: BuildConfig,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableEnteredPassword = ResettableStateFlow(buildConfig.defaultPassword)
  private val mutableEnteredApiKey = ResettableStateFlow(Token(""))
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)
  private val mutableLoginMethods = MutableStateFlow<ImmutableList<LoginMethod>>(persistentListOf())
  private val mutableSelectedLoginMethod = MutableStateFlow(LoginMethod.Password)
  private val mutableOpenIdFirstLogin = MutableStateFlow(OpenIdFirstLogin.NotFirst)

  private val mutableEvents =
    MutableSharedFlow<LoginEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  private var loginJob: Job? = null
  private var timeoutJob: Job? = null

  val events: SharedFlow<LoginEvent> = mutableEvents.asSharedFlow()
  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()
  val enteredApiKey: StateFlow<Token> = mutableEnteredApiKey.asStateFlow()
  val versions: StateFlow<AktualVersions> = versionsStateHolder.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()
  val loginMethods: StateFlow<ImmutableList<LoginMethod>> = mutableLoginMethods.asStateFlow()
  val selectedLoginMethod: StateFlow<LoginMethod> = mutableSelectedLoginMethod.asStateFlow()
  val openIdFirstLogin: StateFlow<OpenIdFirstLogin> = mutableOpenIdFirstLogin.asStateFlow()

  val loginFailure: StateFlow<LoginResult.Failure?> =
    combine(mutableLoginFailure, isLoading) { failure, loading -> if (loading) null else failure }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val token: Flow<Token> = preferences.token.asFlow().filterNotNull()

  init {
    viewModelScope.launch {
      val availableLoginMethods = loginRequester.fetchLoginMethods().filter { it.method != Unknown }
      // Servers don't list API keys as a login method, so always offer it
      val serverMethods =
        availableLoginMethods.map { it.method }.ifEmpty { listOf(LoginMethod.Password) }
      val loginMethods = (serverMethods + LoginMethod.ApiKey).distinct().toImmutableList()
      mutableLoginMethods.update { loginMethods }
      val firstActive = availableLoginMethods.firstOrNull { it.isActive }
      if (firstActive != null) {
        mutableSelectedLoginMethod.update { firstActive.method }
      }
      if (OpenId in loginMethods) {
        val firstLogin = loginRequester.openIdFirstLogin(availableLoginMethods)
        mutableOpenIdFirstLogin.update { firstLogin }
      }
    }

    viewModelScope.launch {
      loginRequester.openIdTokens.collect { token ->
        mutableIsLoading.update { true }
        val result = loginRequester.logIn(token)
        mutableIsLoading.update { false }
        if (result is Failure) mutableLoginFailure.update { result }
      }
    }
  }

  fun clearState() {
    mutableEnteredPassword.reset()
    mutableEnteredApiKey.reset()
    mutableIsLoading.reset()
    mutableLoginFailure.reset()
  }

  fun onEnterPassword(password: String) {
    mutableEnteredPassword.update { Password(password) }
  }

  fun onEnterApiKey(apiKey: String) {
    mutableEnteredApiKey.update { Token(apiKey.trim()) }
  }

  fun onSelectLoginMethod(method: LoginMethod) {
    mutableSelectedLoginMethod.update { method }
    mutableLoginFailure.reset()
  }

  fun onClickSignIn(loginMethod: LoginMethod = mutableSelectedLoginMethod.value) {
    log.v { "onClickSignIn with method=$loginMethod" }
    val password = mutableEnteredPassword.value
    val apiKey = mutableEnteredApiKey.value
    if (loginMethod == LoginMethod.Password && password == Empty) return
    if (loginMethod == ApiKey && apiKey.value.isEmpty()) return
    if (
      loginMethod == OpenId &&
        mutableOpenIdFirstLogin.value == FirstWithPassword &&
        password == Empty
    ) {
      return
    }
    mutableIsLoading.update { true }
    mutableLoginFailure.reset()

    cancelJobs()
    loginJob = viewModelScope.launch {
      val result =
        if (loginMethod == ApiKey) {
          loginRequester.logIn(apiKey)
        } else {
          loginRequester.logIn(password, loginMethod)
        }

      timeoutJob?.cancel()
      timeoutJob = null

      log.d { "Login result = $result" }
      mutableIsLoading.update { false }

      when (result) {
        is Failure -> mutableLoginFailure.update { result }
        is Redirect -> mutableEvents.tryEmit(LoginEvent.Redirect(result.url))
        is Success -> Unit // handled via token flow
      }
    }
    timeoutJob = viewModelScope.launch {
      delay(5.seconds)
      log.w { "Login timed out" }
      mutableIsLoading.update { false }
      mutableEvents.tryEmit(Timeout)
      cancelJobs()
    }
  }

  private fun cancelJobs() {
    loginJob?.cancel()
    loginJob = null
    timeoutJob?.cancel()
    timeoutJob = null
  }
}
