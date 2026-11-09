package aktual.core.connection

import aktual.api.client.HealthApi
import aktual.api.model.health.GetHealthResponse
import aktual.core.model.ForegroundStateHolder
import aktual.core.model.PingStateHolder
import aktual.di.ServerChosenCoroutineScope
import alakazam.kotlin.InfiniteLoopController
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class ServerPingerTest {
  private lateinit var pinger: ServerPinger
  private lateinit var pingStateHolder: PingStateHolder
  private lateinit var foregroundState: ForegroundStateHolder
  private lateinit var healthApi: HealthApi

  @BeforeTest
  fun before() {
    healthApi = mockk()
    coEvery { healthApi.getHealth() } returns GetHealthResponse()
    pingStateHolder = PingStateHolder()
    foregroundState = ForegroundStateHolder()
  }

  @AfterTest
  fun after() {
    pinger.close()
  }

  private fun TestScope.build() {
    pinger =
      ServerPinger(
        scope = ServerChosenCoroutineScope(backgroundScope),
        healthApi = healthApi,
        serverUrl = Demo,
        pingStateHolder = pingStateHolder,
        foregroundState = foregroundState,
        loopController = InfiniteLoopController,
      )
  }

  @Test
  fun `Pings while in foreground`() = runTest {
    build()
    pinger.initialize()

    advanceTimeBy(13.seconds)
    runCurrent()

    coVerify(exactly = 3) { healthApi.getHealth() }
  }

  @Test
  fun `Doesn't ping while in background`() = runTest {
    foregroundState.update { false }
    build()
    pinger.initialize()

    advanceTimeBy(1.minutes)
    runCurrent()

    coVerify(exactly = 0) { healthApi.getHealth() }
  }

  @Test
  fun `Stops pinging when backgrounded and resumes when foregrounded`() = runTest {
    build()
    pinger.initialize()
    advanceTimeBy(3.seconds)
    runCurrent()
    coVerify(exactly = 1) { healthApi.getHealth() }

    foregroundState.update { false }
    advanceTimeBy(1.minutes)
    runCurrent()
    coVerify(exactly = 1) { healthApi.getHealth() }

    foregroundState.update { true }
    advanceTimeBy(3.seconds)
    runCurrent()
    coVerify(exactly = 2) { healthApi.getHealth() }
  }
}
