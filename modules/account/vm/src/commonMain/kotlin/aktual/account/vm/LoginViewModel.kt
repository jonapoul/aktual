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

import aktual.account.domain.LoginRequester
import aktual.account.domain.LoginResult
import aktual.core.di.ViewModelKey
import aktual.core.di.ViewModelScope
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.LoginToken
import aktual.core.model.Password
import aktual.core.model.ServerUrl
import aktual.prefs.AppGlobalPreferences
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
  versionsStateHolder: AktualVersionsStateHolder,
  preferences: AppGlobalPreferences,
  buildConfig: BuildConfig,
) : ViewModel() {
  private val mutableEnteredPassword = ResettableStateFlow(buildConfig.defaultPassword)
  private val mutableIsLoading = ResettableStateFlow(false)
  private val mutableLoginFailure = ResettableStateFlow<LoginResult.Failure?>(null)

  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()
  val versions: StateFlow<AktualVersions> = versionsStateHolder.asStateFlow()
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
