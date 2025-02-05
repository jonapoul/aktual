package actual.core.connection

import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.core.coroutines.CoroutineContexts
import actual.core.state.ActualVersionsStateHolder
import alakazam.kotlin.core.LoopController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class ServerVersionFetcher @Inject constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val versionsStateHolder: ActualVersionsStateHolder,
  private val loopController: LoopController,
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
      Timber.v("fetchVersion %s", apis)
      try {
        val response = withContext(contexts.io) { apis.base.info() }
        Timber.v("Fetched %s", response)
        versionsStateHolder.set(response.build.version)
        break
      } catch (e: CancellationException) {
        throw e
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
