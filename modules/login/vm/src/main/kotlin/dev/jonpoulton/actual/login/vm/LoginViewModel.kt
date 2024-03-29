package dev.jonpoulton.actual.login.vm

import alakazam.android.core.IBuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.core.connection.ServerVersionFetcher
import dev.jonpoulton.actual.core.coroutines.ResettableStateFlow
import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.core.model.Password
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.login.prefs.LoginPreferences
import dev.jonpoulton.actual.serverurl.prefs.ServerUrlPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject internal constructor(
  private val buildConfig: IBuildConfig,
  private val serverVersionFetcher: ServerVersionFetcher,
  private val loginRequester: LoginRequester,
  serverUrlPrefs: ServerUrlPreferences,
  loginPrefs: LoginPreferences,
) : ViewModel() {
  private val mutableEnteredPassword = ResettableStateFlow(Password.Empty)
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)

  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()

  val versions: StateFlow<ActualVersions> = serverVersionFetcher
    .serverVersion
    .map(::versions)
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = versions(serverVersion = null))

  val serverUrl: StateFlow<ServerUrl> = serverUrlPrefs
    .url
    .asFlow()
    .filterNotNull()
    .stateIn(viewModelScope, SharingStarted.Eagerly, ServerUrl.Demo)

  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val loginFailure: StateFlow<LoginResult.Failure?> =
    combine(mutableLoginFailure, isLoading) { failure, loading -> if (loading) null else failure }
      .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val shouldStartSyncing: Flow<Boolean> = loginPrefs.token.asFlow().map { it != null }

  init {
    viewModelScope.launch {
      serverVersionFetcher.startFetching()
    }
  }

  fun clearState() {
    mutableEnteredPassword.reset()
    mutableIsLoading.reset()
  }

  fun onPasswordEntered(password: String) {
    mutableEnteredPassword.update { Password(password) }
  }

  fun onClickSignIn() {
    Timber.v("onClickSignIn")
    mutableIsLoading.update { true }
    viewModelScope.launch {
      val password = mutableEnteredPassword.value
      val result = loginRequester.logIn(password)

      Timber.d("Login result = $result")
      mutableIsLoading.update { false }

      when (result) {
        is LoginResult.Success -> mutableLoginFailure.reset()
        is LoginResult.Failure -> mutableLoginFailure.update { result }
      }
    }
  }

  private fun versions(serverVersion: String?) = ActualVersions(app = buildConfig.versionName, serverVersion)
}
