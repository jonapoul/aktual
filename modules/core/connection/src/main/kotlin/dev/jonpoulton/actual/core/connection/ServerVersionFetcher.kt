package dev.jonpoulton.actual.core.connection

import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.LoopController
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.core.state.ActualVersionsStateHolder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ServerVersionFetcher @Inject constructor(
  private val io: IODispatcher,
  private val apisStateHolder: ActualApisStateHolder,
  private val versionsStateHolder: ActualVersionsStateHolder,
  private val loopController: LoopController,
) {
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
        versionsStateHolder.set(response.build.version)
        break
      } catch (e: CancellationException) {
        Timber.v("Coroutine cancelled")
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
