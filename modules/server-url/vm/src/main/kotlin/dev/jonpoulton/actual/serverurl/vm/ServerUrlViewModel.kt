package dev.jonpoulton.actual.serverurl.vm

import alakazam.android.core.IBuildConfig
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ServerUrlViewModel @Inject constructor(
  buildConfig: IBuildConfig,
) : ViewModel() {
  val serverVersion: Flow<String?> = flowOf("v24.3.0")
  val appVersion: StateFlow<String> = MutableStateFlow(buildConfig.versionName)

  private val mutableEnteredUrl = MutableStateFlow(value = "")
  val enteredUrl: StateFlow<String> = mutableEnteredUrl.asStateFlow()

  fun onUrlEntered(url: String) {
    mutableEnteredUrl.update { url }
  }
}
