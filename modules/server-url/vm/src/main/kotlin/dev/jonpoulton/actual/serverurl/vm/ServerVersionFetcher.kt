package dev.jonpoulton.actual.serverurl.vm

import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.LoopController
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

internal class ServerVersionFetcher @Inject constructor(
  private val io: IODispatcher,
  private val apisStateHolder: ActualApisStateHolder,
  private val loopController: LoopController,
) {
  private val mutableServerVersion = MutableStateFlow<String?>(value = null)
  val serverVersion: StateFlow<String?> = mutableServerVersion.asStateFlow()

  suspend fun startFetching() {
    apisStateHolder.state.collectLatest { apis ->
      if (apis != null) {
        fetchVersion(apis)
      }
    }
  }

  private suspend fun fetchVersion(apis: ActualApis) {
    while (loopController.shouldLoop()) {
      Timber.v("fetchVersion $apis")
      try {
        val response = withContext(io) { apis.base.info() }
        Timber.v("Fetched $response")
        mutableServerVersion.update { response.build.version }
        break
      } catch (e: Exception) {
        Timber.w(e, "Failed fetching server info")
        delay(RETRY_DELAY)
      }
    }
  }

  private companion object {
    val RETRY_DELAY = 3.seconds
  }
}
