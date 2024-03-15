package dev.jonpoulton.actual.serverurl.vm

import alakazam.android.core.IBuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServerUrlViewModel @Inject constructor(
  buildConfig: IBuildConfig,
) : ViewModel() {
  private val mutableIsLoading = MutableStateFlow(value = false)
  private val mutableEnteredUrl = MutableStateFlow(value = "")
  private val mutableConfirmResult = MutableStateFlow<ConfirmResult?>(value = null)

  val serverVersion: StateFlow<String?> = MutableStateFlow("v24.3.0")
  val appVersion: StateFlow<String> = MutableStateFlow(buildConfig.versionName)

  val enteredUrl: StateFlow<String> = mutableEnteredUrl.asStateFlow()
  val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

  val shouldNavigate: Flow<Boolean> = mutableConfirmResult.map { value -> value is ConfirmResult.Succeeded }
  val errorMessage: Flow<String?> = mutableConfirmResult.map { value -> (value as? ConfirmResult.Failed)?.reason }

  fun onUrlEntered(url: String) {
    mutableEnteredUrl.update { url }
  }

  fun onClickConfirm() {
    Timber.d("onClickConfirm")
    mutableIsLoading.update { true }
    viewModelScope.launch {
      delay(timeMillis = 2_000L)
      mutableIsLoading.update { false }
    }
  }
}
