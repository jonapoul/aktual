/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.core.connection

import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.api.client.BaseApi
import actual.api.model.base.Build
import actual.api.model.base.InfoResponse
import actual.core.model.ActualVersions
import actual.core.model.ActualVersionsStateHolder
import actual.test.TestBuildConfig
import actual.test.assertThatNextEmissionIsEqualTo
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
  private lateinit var apisStateHolder: ActualApisStateHolder
  private lateinit var versionsStateHolder: ActualVersionsStateHolder

  // mock
  private lateinit var apis: ActualApis
  private lateinit var baseApi: BaseApi

  @Before
  fun before() {
    baseApi = mockk()
    apis = mockk {
      every { base } returns baseApi
      every { close() } just runs
    }

    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.update { apis }
    versionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)
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

  private fun emptyState() = ActualVersions(TestBuildConfig.versionName, server = null)
}
