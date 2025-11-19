/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.connection

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.BaseApi
import aktual.api.model.base.Build
import aktual.api.model.base.InfoResponse
import aktual.core.model.AktualVersions
import aktual.core.model.AktualVersionsStateHolder
import aktual.test.TestBuildConfig
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.kotlin.core.LoopController
import alakazam.test.core.FiniteLoopController
import alakazam.test.core.SingleLoopController
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import java.io.IOException
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ServerVersionFetcherTest {
  // real
  private lateinit var fetcher: ServerVersionFetcher
  private lateinit var apisStateHolder: AktualApisStateHolder
  private lateinit var versionsStateHolder: AktualVersionsStateHolder

  // mock
  private lateinit var apis: AktualApis
  private lateinit var baseApi: BaseApi

  @Before
  fun before() {
    baseApi = mockk()
    apis = mockk {
      every { base } returns baseApi
      every { close() } just runs
    }

    apisStateHolder = AktualApisStateHolder()
    apisStateHolder.update { apis }
    versionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)
  }

  private fun TestScope.build(loopController: LoopController = SingleLoopController()) {
    fetcher = ServerVersionFetcher(
      contexts = TestCoroutineContexts(unconfinedDispatcher),
      apisStateHolder = apisStateHolder,
      versionsStateHolder = versionsStateHolder,
      loopController = loopController,
    )
  }

  @Test
  fun `No APIs means nothing is fetched`() = runTest(timeout = 5.seconds) {
    // Given
    build()
    apisStateHolder.reset()
    advanceUntilIdle()

    // When
    val fetchJob = launch { fetcher.startFetching() }

    advanceUntilIdle()
    versionsStateHolder.test {
      // Then
      assertThatNextEmissionIsEqualTo(emptyState())
      advanceUntilIdle()

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
      fetchJob.cancel()
    }
  }

  @Test
  fun `Valid fetch response`() = runTest(timeout = 5.seconds) {
    // Given
    build()
    coEvery { baseApi.fetchInfo() } returns InfoResponse(
      build = Build(
        name = "ABC",
        description = "XYZ",
        version = "1.2.3",
      ),
    )

    // When
    versionsStateHolder.test {
      assertThatNextEmissionIsEqualTo(emptyState())

      val fetchJob = launch { fetcher.startFetching() }

      // Then
      assertThat(awaitItem().server).isEqualTo("1.2.3")
      cancelAndIgnoreRemainingEvents()
      fetchJob.cancel()
    }
  }

  @Test
  @Ignore("Fails if you run the full test suite, passes otherwise")
  fun `Failed then successful fetch response`() = runTest(timeout = 5.seconds) {
    // Given
    val validResponse = InfoResponse(build = Build(name = "ABC", description = "XYZ", version = "1.2.3"))
    val failureReason = "SOMETHING BROKE"
    coEvery { baseApi.fetchInfo() } answers {
      coEvery { baseApi.fetchInfo() } returns validResponse
      throw IOException(failureReason)
    }

    val loopController = FiniteLoopController(maxLoops = 2)
    build(loopController)

    // When
    versionsStateHolder.test {
      assertThatNextEmissionIsEqualTo(emptyState())

      val fetchJob = launch { fetcher.startFetching() }

      // Then
      assertThat(awaitItem().server).isEqualTo("1.2.3")
      cancelAndIgnoreRemainingEvents()
      fetchJob.cancel()
    }
  }

  private fun emptyState() = AktualVersions(TestBuildConfig.versionName, server = null)
}
