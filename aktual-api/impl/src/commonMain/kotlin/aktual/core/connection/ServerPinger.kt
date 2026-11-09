package aktual.core.connection

import aktual.api.client.HealthApi
import aktual.core.model.ForegroundStateHolder
import aktual.core.model.PingState
import aktual.core.model.PingStateHolder
import aktual.core.model.ServerUrl
import aktual.core.utils.runCatchingCancellable
import aktual.di.Closeable
import aktual.di.Initializable
import aktual.di.ServerChosenCoroutineScope
import aktual.di.ServerChosenScope
import alakazam.kotlin.LoopController
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@SingleIn(ServerChosenScope::class)
@ContributesIntoSet(
  ServerChosenScope::class,
  binding<@ForScope(ServerChosenScope::class) Initializable>(),
)
@ContributesIntoSet(
  ServerChosenScope::class,
  binding<@ForScope(ServerChosenScope::class) Closeable>(),
)
class ServerPinger(
  private val scope: ServerChosenCoroutineScope,
  private val healthApi: HealthApi,
  private val serverUrl: ServerUrl,
  private val pingStateHolder: PingStateHolder,
  private val foregroundState: ForegroundStateHolder,
  private val loopController: LoopController,
) : Initializable, Closeable {
  private var job: Job? = null
  private var consecutiveFailures = 0
  private var lastSuccess: TimeMark? = null

  override fun initialize() {
    logcat.d { "initialize: pinging $serverUrl" }
    job?.cancel()
    job = scope.launch {
      // Android blocks network access for backgrounded apps, so pings would just fail
      foregroundState.collectLatest { isForeground ->
        if (isForeground) {
          pingLoop()
        } else {
          logcat.d { "In background, pausing pings" }
        }
      }
    }
  }

  override fun close() {
    logcat.d { "close: stopped pinging $serverUrl" }
    job?.cancel()
    job = null
    pingStateHolder.update { Unknown }
  }

  private suspend fun pingLoop() {
    while (loopController.shouldLoop()) {
      delay(BEFORE_DELAY)
      val state = attemptPing(healthApi)
      val previous = pingStateHolder.value
      if (state != previous) logcat.d { "Ping state $previous -> $state" }
      pingStateHolder.update { state }
      delay(AFTER_DELAY)
    }
  }

  private suspend fun attemptPing(healthApi: HealthApi): PingState {
    return runCatchingCancellable<PingState>(
      attempt = {
        val response = healthApi.getHealth()
        logcat.v { "Succeeded pinging server: $response" }
        if (consecutiveFailures > 0) {
          logcat.d { "Ping recovered after $consecutiveFailures failures" }
        }
        consecutiveFailures = 0
        lastSuccess = TimeSource.Monotonic.markNow()
        if (response.status == "UP") Success else Unknown
      },
      onFailure = { e ->
        consecutiveFailures++
        logcat.w(e) {
          "Failed pinging $serverUrl (failure #$consecutiveFailures, last success " +
            "${lastSuccess?.elapsedNow()?.let { "$it ago" } ?: "never"}): ${e.causeChain()}"
        }
        Failure
      },
    )
  }

  private fun Throwable.causeChain(): String =
    generateSequence(this) { it.cause }
      .take(MAX_CAUSES)
      .joinToString(" <- ") { "${it::class.simpleName}(${it.message})" }

  private companion object {
    const val MAX_CAUSES = 5

    // after the first call, effectively a minimum 5sec delay between calls
    val BEFORE_DELAY = 2.seconds
    val AFTER_DELAY = 3.seconds
  }
}
