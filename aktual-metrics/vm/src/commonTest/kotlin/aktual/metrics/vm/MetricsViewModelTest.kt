package aktual.metrics.vm

import aktual.api.client.MetricsApi
import aktual.api.model.metrics.GetMetricsResponse
import aktual.core.model.bytes
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.TestClock
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException

class MetricsViewModelTest {
  private lateinit var metricsApi: MetricsApi
  private lateinit var viewModel: MetricsViewModel

  @BeforeTest
  fun before() {
    metricsApi = mockk()
  }

  private fun buildViewModel() {
    viewModel =
      MetricsViewModel(
        metricsApi = metricsApi,
        contexts = TestCoroutineContexts(EmptyCoroutineContext),
        clock = TestClock(EXAMPLE_INSTANT),
      )
  }

  @Test
  fun `Fetch data on load and refresh`() = runTest {
    // given
    coEvery { metricsApi.getMetrics() } coAnswers
      {
        delay(200.milliseconds)
        GetMetricsResponse(EXAMPLE_MEMORY, EXAMPLE_UPTIME)
      }

    // when
    buildViewModel()
    viewModel.state.test {
      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(
        MetricsState.Success(EXAMPLE_MEMORY, EXAMPLE_UPTIME, EXAMPLE_INSTANT)
      )
      coVerify(exactly = 1) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      // when
      viewModel.refresh()

      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(
        MetricsState.Success(EXAMPLE_MEMORY, EXAMPLE_UPTIME, EXAMPLE_INSTANT)
      )
      coVerify(exactly = 2) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Disconnected on IO error`() = runTest {
    // given
    coEvery { metricsApi.getMetrics() } coAnswers
      {
        delay(200.milliseconds)
        throw IOException("Connection error")
      }

    // when
    buildViewModel()
    viewModel.state.test {
      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(MetricsState.Disconnected)
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }

    coVerify(exactly = 1) { metricsApi.getMetrics() }
  }

  @Test
  fun `Handle serialization exception`() = runTest {
    // given
    coEvery { metricsApi.getMetrics() } throws SerializationException("Failed lol")

    // when
    buildViewModel()
    viewModel.state.test {
      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(MetricsState.Failure(cause = "Failed lol"))
      coVerify(exactly = 1) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      // when
      coEvery { metricsApi.getMetrics() } throws IOException("Connection problem")
      viewModel.refresh()

      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(MetricsState.Disconnected)
      coVerify(exactly = 2) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      cancelAndIgnoreRemainingEvents()
    }
  }

  private companion object {
    // Mon Dec 08 2025 16:37:13.825
    private val EXAMPLE_INSTANT = Instant.fromEpochMilliseconds(1765211833825L)
    private val EXAMPLE_UPTIME = 123.days + 4.hours + 5.seconds + 678.milliseconds
    private val EXAMPLE_MEMORY =
      GetMetricsResponse.Memory(
        rss = 112377856L.bytes,
        heapTotal = 32088064L.bytes,
        heapUsed = 29558608L.bytes,
        external = 3925760L.bytes,
        arrayBuffers = 387791L.bytes,
      )
  }
}
