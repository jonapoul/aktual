package aktual.metrics.vm

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.MetricsApi
import aktual.api.model.metrics.GetMetricsResponse
import aktual.core.model.ServerUrl
import aktual.core.model.bytes
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.core.TestCoroutineContexts
import app.cash.turbine.test
import assertk.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MetricsViewModelTest {
  private lateinit var metricsApi: MetricsApi
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var viewModel: MetricsViewModel

  @BeforeTest
  fun before() {
    metricsApi = mockk()
    apisStateHolder = AktualApisStateHolder()
  }

  private fun buildViewModel() {
    viewModel = MetricsViewModel(
      apisStateHolder = apisStateHolder,
      contexts = TestCoroutineContexts(EmptyCoroutineContext),
    )
  }

  @Test
  fun `Fetch data on load and refresh`() = runTest {
    // given
    apisStateHolder.update { buildApis() }
    coEvery { metricsApi.getMetrics() } coAnswers {
      delay(200.milliseconds)
      GetMetricsResponse(EXAMPLE_MEMORY, EXAMPLE_UPTIME)
    }

    // when
    buildViewModel()
    viewModel.state.test {
      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(MetricsState.Success(EXAMPLE_MEMORY, EXAMPLE_UPTIME))
      coVerify(exactly = 1) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      // when
      viewModel.refresh()

      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(MetricsState.Success(EXAMPLE_MEMORY, EXAMPLE_UPTIME))
      coVerify(exactly = 2) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Disconnected when no APIs available`() = runTest {
    // given
    apisStateHolder.update { buildApis(metricsApi = null) }

    // when
    buildViewModel()
    viewModel.state.test {
      // then
      assertThatNextEmissionIsEqualTo(MetricsState.Loading)
      assertThatNextEmissionIsEqualTo(MetricsState.Disconnected)
      ensureAllEventsConsumed()
      cancelAndIgnoreRemainingEvents()
    }

    coVerify(exactly = 0) { metricsApi.getMetrics() }
  }

  @Test
  fun `Handle serialization exception`() = runTest {
    // given
    apisStateHolder.update { buildApis() }
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
      assertThatNextEmissionIsEqualTo(MetricsState.Failure(cause = "Connection problem"))
      coVerify(exactly = 2) { metricsApi.getMetrics() }
      ensureAllEventsConsumed()

      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun buildApis(
    metricsApi: MetricsApi? = this.metricsApi,
  ) = metricsApi?.let {
    AktualApis(
      serverUrl = SERVER_URL,
      client = mockk(),
      account = mockk(),
      base = mockk(),
      health = mockk(),
      metrics = it,
      sync = mockk(),
      syncDownload = mockk(),
    )
  }

  private companion object {
    private val SERVER_URL = ServerUrl("https://server.com/actual-budget")

    private val EXAMPLE_UPTIME = 123.days + 4.hours + 5.seconds + 678.milliseconds
    private val EXAMPLE_MEMORY = GetMetricsResponse.Memory(
      rss = 112377856L.bytes,
      heapTotal = 32088064L.bytes,
      heapUsed = 29558608L.bytes,
      external = 3925760L.bytes,
      arrayBuffers = 387791L.bytes,
    )
  }
}
