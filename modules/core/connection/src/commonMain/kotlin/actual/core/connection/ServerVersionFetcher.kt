package actual.core.connection

import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.core.versions.ActualVersionsStateHolder
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.Logger
import alakazam.kotlin.core.LoopController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ServerVersionFetcher @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val versionsStateHolder: ActualVersionsStateHolder,
  private val loopController: LoopController,
  private val logger: Logger,
) {
  suspend fun startFetching() {
    apisStateHolder.collectLatest { apis ->
      if (apis != null) {
        fetchVersion(apis)
      }
    }
  }

  private suspend fun fetchVersion(apis: ActualApis) {
    while (loopController.shouldLoop()) {
      logger.v("fetchVersion %s", apis)
      try {
        val response = withContext(contexts.io) { apis.base.info() }
        val body = response.body()
        if (response.isSuccessful && body != null) {
          logger.v("Fetched %s", body)
          versionsStateHolder.set(body.build.version)
        } else {
          logger.e("Failure response fetching server info: $response")
        }
        break
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logger.w(e, "Failed fetching server info")
        delay(RETRY_DELAY)
      }
    }
  }

  private companion object {
    val RETRY_DELAY = 3.seconds
  }
}
