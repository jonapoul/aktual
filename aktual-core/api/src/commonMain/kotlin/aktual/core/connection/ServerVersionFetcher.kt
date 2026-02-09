package aktual.core.connection

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.core.model.AktualVersionsStateHolder
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.LoopController
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ResponseException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
class ServerVersionFetcher(
    private val contexts: CoroutineContexts,
    private val apisStateHolder: AktualApisStateHolder,
    private val versionsStateHolder: AktualVersionsStateHolder,
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

  private suspend fun fetchVersion(apis: AktualApis) {
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
