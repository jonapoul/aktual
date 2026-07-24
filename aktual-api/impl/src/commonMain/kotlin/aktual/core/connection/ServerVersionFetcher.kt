package aktual.core.connection

import aktual.api.client.BaseApi
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.PingState
import aktual.core.model.PingStateHolder
import aktual.di.Closeable
import aktual.di.Initializable
import aktual.di.ScopeLifecycle
import aktual.di.ServerChosenScope
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.LoopController
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.ktor.client.plugins.ResponseException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat

@SingleIn(ServerChosenScope::class)
@ContributesIntoSet(ServerChosenScope::class, binding<Initializable>())
@ContributesIntoSet(ServerChosenScope::class, binding<Closeable>())
class ServerVersionFetcher(
  private val scope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val baseApi: BaseApi,
  private val versionsStateHolder: AktualVersionsStateHolder,
  private val loopController: LoopController,
  private val pingStateHolder: PingStateHolder,
) : ScopeLifecycle {
  private var job: Job? = null

  override fun initialize() {
    logcat.v { "initialize" }
    job = scope.launch {
      pingStateHolder.collectLatest { state ->
        logcat.v { "ServerVersionFetcher collected $state" }
        if (state == PingState.Success) {
          fetchVersionUntilSuccess()
        } else {
          versionsStateHolder.set(null)
        }
      }
    }
  }

  override fun close() {
    logcat.v { "close" }
    job?.cancel()
    job = null
  }

  private suspend fun fetchVersionUntilSuccess() {
    while (loopController.shouldLoop()) {
      logcat.v { "fetchVersion" }
      try {
        val response = withContext(contexts.io) { baseApi.fetchInfo() }
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
