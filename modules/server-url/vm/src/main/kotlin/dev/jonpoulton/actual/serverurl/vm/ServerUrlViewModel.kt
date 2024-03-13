package dev.jonpoulton.actual.serverurl.vm

import alakazam.android.core.IBuildConfig
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class ServerUrlViewModel @Inject constructor(
  buildConfig: IBuildConfig,
) : ViewModel() {
  val serverVersion: Flow<String> = flowOf("v24.3.0")
  val appVersion: Flow<String> = flowOf(buildConfig.versionName)
}
