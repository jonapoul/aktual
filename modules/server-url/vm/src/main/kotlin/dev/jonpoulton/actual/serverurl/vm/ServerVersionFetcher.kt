package dev.jonpoulton.actual.serverurl.vm

import alakazam.kotlin.core.IODispatcher
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class ServerVersionFetcher @Inject constructor(
  private val io: IODispatcher,
  private val apiStateHolder: ActualApisStateHolder,
) {
  private val mutableServerVersion = MutableStateFlow<String?>(value = null)
  val serverVersion: StateFlow<String?> = mutableServerVersion.asStateFlow()

  suspend fun startFetching() {
    apiStateHolder.state.collectLatest { apis ->
      if (apis != null) {
        fetchVersion(apis)
      }
    }
  }

  private suspend fun fetchVersion(apis: ActualApis) {
    try {
      val response = withContext(io) { apis.base.info() }
      Timber.v("Fetched $response")
      mutableServerVersion.update { response.build.version }
    } catch (e: Exception) {
      Timber.w(e, "Failed fetching info")
    }
  }
}
