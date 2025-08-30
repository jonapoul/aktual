package actual.core.connection

import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.core.model.ActualVersionsStateHolder
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.LoopController
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import logcat.logcat
import kotlin.time.Duration.Companion.seconds

@Inject
class ServerVersionFetcher(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val versionsStateHolder: ActualVersionsStateHolder,
  private val loopController: LoopController,
) {
  suspend fun startFetching() {
    logcat.v { "startFetching" }
    apisStateHolder.collectLatest { apis ->
      logcat.v { "startFetching collected $apis" }
      if (apis != null) {
        fetchVersion(apis)
      }
    }
  }

  private suspend fun fetchVersion(apis: ActualApis) {
    while (loopController.shouldLoop()) {
      logcat.v { "fetchVersion %s".format(apis) }
      try {
        val response = withContext(contexts.io) { apis.base.fetchInfo() }
        logcat.v { "Fetched %s".format(response) }
        versionsStateHolder.set(response.build.version)
        break
      } catch (e: CancellationException) {
        throw e
      } catch (e: ResponseException) {
        logcat.w(e) { "HTTP failure fetching server info" }
        delay(RETRY_DELAY)
      } catch (e: Exception) {
        logcat.w(e) { "Failed fetching server info" }
        delay(RETRY_DELAY)
      }
    }
  }

  private companion object {
    val RETRY_DELAY = 3.seconds
  }
}
