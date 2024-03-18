package dev.jonpoulton.actual.serverurl.vm

import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.LoopController
import alakazam.test.core.CoroutineRule
import alakazam.test.core.FiniteLoopController
import alakazam.test.core.SingleLoopController
import app.cash.turbine.test
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.api.client.BaseApi
import dev.jonpoulton.actual.api.model.base.Build
import dev.jonpoulton.actual.api.model.base.InfoResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class ServerVersionFetcherTest {
  @get:Rule
  val coroutineRule = CoroutineRule()

  @get:Rule
  val timberRule = TimberTestRule.logAllWhenTestFails()!!

  // real
  private lateinit var fetcher: ServerVersionFetcher
  private lateinit var apisStateHolder: ActualApisStateHolder

  // mock
  private lateinit var apis: ActualApis
  private lateinit var baseApi: BaseApi

  @Before
  fun before() {
    baseApi = mockk()
    apis = mockk { every { base } returns baseApi }

    apisStateHolder = ActualApisStateHolder()
    apisStateHolder.set(apis)

    build()
  }

  private fun build(
    loopController: LoopController = SingleLoopController(),
  ) {
    fetcher = ServerVersionFetcher(
      io = IODispatcher(coroutineRule.dispatcher),
      apisStateHolder = apisStateHolder,
      loopController = loopController,
    )
  }

  @Test
  fun `No APIs means nothing is fetched`() = runTest(timeout = 5.seconds) {
    // Given
    apisStateHolder.reset()
    coroutineRule.advanceUntilIdle()

    // When
    val fetchJob = launch { fetcher.startFetching() }

    coroutineRule.advanceUntilIdle()
    fetcher.serverVersion.test {
      // Then
      assertNull(awaitItem())
      cancelAndIgnoreRemainingEvents()
      fetchJob.cancel()
    }
  }

  @Test
  fun `Valid fetch response`() = runTest(timeout = 5.seconds) {
    // Given
    coEvery { baseApi.info() } returns InfoResponse(
      build = Build(
        name = "ABC",
        description = "XYZ",
        version = "1.2.3",
      ),
    )

    // When
    fetcher.serverVersion.test {
      assertNull(awaitItem())

      val fetchJob = launch { fetcher.startFetching() }

      // Then
      assertEquals(expected = "1.2.3", actual = awaitItem())
      cancelAndIgnoreRemainingEvents()
      fetchJob.cancel()
    }
  }

  @Test
  fun `Failed then successful fetch response`() = runTest(timeout = 5.seconds) {
    // Given
    val validResponse = InfoResponse(build = Build(name = "ABC", description = "XYZ", version = "1.2.3"))
    val failureReason = "SOMETHING BROKE"
    coEvery { baseApi.info() } answers {
      coEvery { baseApi.info() } returns validResponse
      throw IOException(failureReason)
    }

    val loopController = FiniteLoopController(maxLoops = 2)
    build(loopController)

    // When
    fetcher.serverVersion.test {
      assertNull(awaitItem())

      val fetchJob = launch { fetcher.startFetching() }

      // Then
      assertEquals(expected = "1.2.3", actual = awaitItem())
      cancelAndIgnoreRemainingEvents()
      fetchJob.cancel()
    }
  }
}
