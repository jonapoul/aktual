package aktual.core.connection

import aktual.api.client.HealthApi
import aktual.core.model.PingState
import aktual.core.model.PingStateHolder
import aktual.di.Closeable
import aktual.di.Initializable
import aktual.di.ScopeLifecycle
import aktual.di.ServerChosenScope
import alakazam.kotlin.LoopController
import alakazam.kotlin.launchInfiniteLoop
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import logcat.logcat

@SingleIn(ServerChosenScope::class)
@ContributesIntoSet(ServerChosenScope::class, binding<Initializable>())
@ContributesIntoSet(ServerChosenScope::class, binding<Closeable>())
class ServerPinger(
  private val scope: CoroutineScope,
  private val healthApi: HealthApi,
  private val pingStateHolder: PingStateHolder,
  private val loopController: LoopController,
) : ScopeLifecycle {
  private var job: Job? = null

  override fun initialize() {
    job?.cancel()
    job =
      scope.launchInfiniteLoop(loopController) {
        delay(BEFORE_DELAY)
        val state = attemptPing(healthApi)
        pingStateHolder.update { state }
        delay(AFTER_DELAY)
      }
  }

  override fun close() {
    job?.cancel()
    job = null
    pingStateHolder.update { PingState.Unknown }
  }

  private suspend fun attemptPing(healthApi: HealthApi): PingState {
    return try {
      val response = healthApi.getHealth()
      logcat.v { "Succeeded pinging server: $response" }
      if (response.status == "UP") PingState.Success else PingState.Unknown
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed pinging server" }
      PingState.Failure
    }
  }

  private companion object {
    // after the first call, effectively a minimum 5sec delay between calls
    val BEFORE_DELAY = 2.seconds
    val AFTER_DELAY = 3.seconds
  }
}
