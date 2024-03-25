package dev.jonpoulton.actual.login.vm

import alakazam.android.core.IBuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.core.connection.ServerVersionFetcher
import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.core.model.Password
import dev.jonpoulton.actual.core.model.ServerUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val buildConfig: IBuildConfig,
  private val serverVersionFetcher: ServerVersionFetcher,
) : ViewModel() {
  private val mutableEnteredPassword = MutableStateFlow(Password.Empty)
  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()

  val versions: StateFlow<ActualVersions> = serverVersionFetcher.serverVersion
    .map { serverVersion -> versions(serverVersion) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = versions(serverVersion = null))

  val serverUrl: StateFlow<ServerUrl> = flowOf<ServerUrl>()
    .stateIn(viewModelScope, SharingStarted.Eagerly, ServerUrl.Demo)

  val errorMessage: StateFlow<String?> = flowOf<String?>()
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val shouldStartSyncing: Flow<Boolean> = flowOf(false)

  init {
    viewModelScope.launch {
      serverVersionFetcher.startFetching()
    }
  }

  fun clearState() {
    // TODO
  }

  fun onClickSignIn() {
    // TODO: implement
  }

  private fun versions(serverVersion: String?) = ActualVersions(app = buildConfig.versionName, serverVersion)
}
