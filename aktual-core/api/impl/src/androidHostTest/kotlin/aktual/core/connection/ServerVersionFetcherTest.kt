package aktual.core.connection

import aktual.api.client.BaseApi
import aktual.api.model.base.Build
import aktual.api.model.base.InfoResponse
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.PingState
import aktual.core.model.PingStateHolder
import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.kotlin.LoopController
import alakazam.test.FiniteLoopController
import alakazam.test.SingleLoopController
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class ServerVersionFetcherTest {
  // real
  private lateinit var fetcher: ServerVersionFetcher
  private lateinit var versionsStateHolder: AktualVersionsStateHolder
  private lateinit var pingStateHolder: PingStateHolder

  // mock
  private lateinit var baseApi: BaseApi

  @BeforeTest
  fun before() {
    baseApi = mockk()
    versionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)
    pingStateHolder = PingStateHolder()
  }

  @AfterTest
  fun after() {
    fetcher.close()
  }

  private fun TestScope.build(loopController: LoopController = SingleLoopController()) {
    fetcher =
      ServerVersionFetcher(
        contexts = TestCoroutineContexts(unconfinedDispatcher),
        baseApi = baseApi,
        versionsStateHolder = versionsStateHolder,
        pingStateHolder = pingStateHolder,
        loopController = loopController,
        scope = this,
      )
  }

  @Test
  fun `Nothing fetched when not pinging`() = runTest {
    // Given
    build()
    fetcher.initialize()
    advanceUntilIdle()

    // When / Then
    versionsStateHolder.test {
      assertThatNextEmissionIsEqualTo(emptyState())
      advanceUntilIdle()
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
    fetcher.close()
  }

  @Test
  fun `Valid fetch response`() = runTest {
    // Given
    build()
    coEvery { baseApi.fetchInfo() } returns
      InfoResponse(build = Build(name = "ABC", description = "XYZ", version = "1.2.3"))

    // When
    versionsStateHolder.test {
      assertThatNextEmissionIsEqualTo(emptyState())

      fetcher.initialize()
      pingStateHolder.update { PingState.Success }

      // Then
      assertThat(awaitItem().server).isEqualTo("1.2.3")
      cancelAndIgnoreRemainingEvents()
    }
    fetcher.close()
  }

  @Test
  @Ignore("Fails if you run the full test suite, passes otherwise")
  fun `Failed then successful fetch response`() = runTest {
    // Given
    val validResponse =
      InfoResponse(build = Build(name = "ABC", description = "XYZ", version = "1.2.3"))
    val failureReason = "SOMETHING BROKE"
    coEvery { baseApi.fetchInfo() } answers
      {
        coEvery { baseApi.fetchInfo() } returns validResponse
        throw IOException(failureReason)
      }

    val loopController = FiniteLoopController(maxLoops = 2)
    build(loopController)

    // When
    versionsStateHolder.test {
      assertThatNextEmissionIsEqualTo(emptyState())

      fetcher.initialize()
      pingStateHolder.update { PingState.Success }

      // Then
      assertThat(awaitItem().server).isEqualTo("1.2.3")
      cancelAndIgnoreRemainingEvents()
    }
    fetcher.close()
  }

  private fun emptyState() = AktualVersions(TestBuildConfig.versionName, server = null)
}
