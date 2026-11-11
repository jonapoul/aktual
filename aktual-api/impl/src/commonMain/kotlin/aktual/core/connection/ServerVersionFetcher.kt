package aktual.core.connection

import aktual.api.client.BaseApi
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.ForegroundStateHolder
import aktual.core.model.PingStateHolder
import aktual.core.utils.runCatchingCancellable
import aktual.di.Closeable
import aktual.di.Initializable
import aktual.di.ServerChosenCoroutineScope
import aktual.di.ServerChosenScope
import aktual.log
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.LoopController
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.ktor.client.plugins.ResponseException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SingleIn(ServerChosenScope::class)
@ContributesIntoSet(
  ServerChosenScope::class,
  binding<@ForScope(ServerChosenScope::class) Initializable>(),
)
@ContributesIntoSet(
  ServerChosenScope::class,
  binding<@ForScope(ServerChosenScope::class) Closeable>(),
)
class ServerVersionFetcher(
  private val scope: ServerChosenCoroutineScope,
  private val contexts: CoroutineContexts,
  private val baseApi: BaseApi,
  private val versionsStateHolder: AktualVersionsStateHolder,
  private val loopController: LoopController,
  private val pingStateHolder: PingStateHolder,
  private val foregroundState: ForegroundStateHolder,
) : Initializable, Closeable {
  private var job: Job? = null

  override fun initialize() {
    log.v { "initialize" }
    job = scope.launch {
      // Fetched again on resume, in case the server was updated while the app was in the
      // background
      combine(pingStateHolder, foregroundState, ::Pair).collectLatest { (state, isForeground) ->
        log.v { "ServerVersionFetcher collected $state, foreground=$isForeground" }
        when {
          state != Success -> versionsStateHolder.set(null)
          isForeground -> fetchVersionUntilSuccess()
        }
      }
    }
  }

  override fun close() {
    log.v { "close" }
    job?.cancel()
    job = null
  }

  private suspend fun fetchVersionUntilSuccess() {
    while (loopController.shouldLoop()) {
      log.v { "fetchVersion" }
      runCatchingCancellable(
        attempt = {
          val response = withContext(contexts.io) { baseApi.fetchInfo() }
          log.v { "Fetched %s".format(response) }
          versionsStateHolder.set(response.build.version)
          break
        },
        onFailure = { e ->
          if (e is ResponseException) {
            log.w(e) { "HTTP failure fetching server info" }
          } else {
            log.w(e) { "Failed fetching server info" }
          }
          delay(RETRY_DELAY)
        },
      )
    }
  }

  private companion object {
    val RETRY_DELAY = 3.seconds
  }
}
