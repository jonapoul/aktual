package dev.jonpoulton.actual.login.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.actual.core.model.ActualVersions
import dev.jonpoulton.actual.core.model.Password
import dev.jonpoulton.actual.core.model.ServerUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
  private val mutableEnteredPassword = MutableStateFlow(Password.Empty)
  val enteredPassword: StateFlow<Password> = mutableEnteredPassword.asStateFlow()

  private val defaultVersions = ActualVersions(app = "1.2.3", server = "2.3.4")
  val versions: StateFlow<ActualVersions> = flowOf(defaultVersions)
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = defaultVersions)

  val serverUrl: StateFlow<ServerUrl> = flowOf<ServerUrl>()
    .stateIn(viewModelScope, SharingStarted.Eagerly, ServerUrl.Demo)

  val errorMessage: StateFlow<String?> = flowOf<String?>()
    .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

  val shouldStartSyncing: Flow<Boolean> = flowOf(false)

  fun onClickSignIn() {
    // TODO: implement
  }
}
