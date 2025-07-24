package actual.core.connection

import actual.api.client.ActualApisStateHolder
import actual.api.client.HealthApi
import actual.core.model.PingState
import actual.core.model.PingStateHolder
import alakazam.kotlin.core.LoopController
import alakazam.kotlin.core.launchInfiniteLoop
import alakazam.kotlin.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class ServerPinger @Inject constructor(
  private val scope: CoroutineScope,
  private val apiStateHolder: ActualApisStateHolder,
  private val pingStateHolder: PingStateHolder,
  private val loopController: LoopController,
) {
  private var job: Job? = null

  fun start() {
    job?.cancel()
    job = scope.launchInfiniteLoop(loopController) {
      // Allows ConnectionMonitor a chance to create the APIs before this is called the first time
      delay(BEFORE_DELAY)

      val healthApi = apiStateHolder.value?.health
      if (healthApi == null) {
        Logger.w("No API - setting ping state to failure")
        pingStateHolder.update { PingState.Failure }
      } else {
        attemptPing(healthApi)
      }

      delay(AFTER_DELAY)
    }
  }

  private suspend fun attemptPing(healthApi: HealthApi) {
    try {
      val response = healthApi.getHealth()
      pingStateHolder.update { PingState.Success }
      Logger.v("Succeeded pinging server: $response")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.w(e, "Failed pinging server")
      pingStateHolder.update { PingState.Failure }
    }
  }

  fun stop() {
    job?.cancel()
    job = null
    pingStateHolder.update { PingState.Unknown }
  }

  private companion object {
    // after the first call, effectively a minimum 5sec delay between calls
    val BEFORE_DELAY = 2.seconds
    val AFTER_DELAY = 3.seconds
  }
}
