/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.connection

import aktual.api.client.AktualApisStateHolder
import aktual.api.client.HealthApi
import aktual.core.model.PingState
import aktual.core.model.PingStateHolder
import alakazam.kotlin.core.LoopController
import alakazam.kotlin.core.launchInfiniteLoop
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import logcat.logcat
import kotlin.time.Duration.Companion.seconds

@Inject
@SingleIn(AppScope::class)
class ServerPinger internal constructor(
  private val scope: CoroutineScope,
  private val apiStateHolder: AktualApisStateHolder,
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
      val state = if (healthApi == null) {
        logcat.w { "No API - setting ping state to failure" }
        PingState.Failure
      } else {
        attemptPing(healthApi)
      }

      pingStateHolder.update { state }
      delay(AFTER_DELAY)
    }
  }

  private suspend fun attemptPing(healthApi: HealthApi): PingState = try {
    val response = healthApi.getHealth()
    logcat.v { "Succeeded pinging server: $response" }
    if (response.status == "UP") PingState.Success else PingState.Unknown
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    logcat.w(e) { "Failed pinging server" }
    PingState.Failure
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
