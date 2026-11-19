package aktual.updates.domain

import aktual.core.model.BuildConfig
import aktual.di.AppCoroutineScope
import aktual.test.TestBuildConfig
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.TestCoroutineContexts
import alakazam.test.unconfinedDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import github.api.client.GithubApiImpl
import github.api.client.GithubJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.intellij.lang.annotations.Language

class NightlyUpdatesImplTest {
  private lateinit var mockEngine: MockEngine.Queue

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Finds a nightly ahead of the installed commit`() = runTest {
    mockEngine += { respondJson(runsJson(sha = NIGHTLY_SHA)) }
    mockEngine += { respondJson(compareJson(status = "ahead", aheadBy = 3)) }
    val updates = buildUpdates(enabled = true)

    updates.latest.test {
      assertThat(awaitItem()).isNull()
      assertThat(awaitItem())
        .isEqualTo(
          NightlyBuild(
            runId = 123,
            gitHash = "dbc10893",
            commitTime = Instant.parse("2026-11-18T21:23:52Z"),
            commitsAhead = 3,
          ),
        )
    }

    assertThat(mockEngine.requestHistory.map { it.url.toString() })
      .isEqualTo(
        listOf(
          "https://api.github.com/repos/jonapoul/aktual/actions/workflows/nightly.yml/runs" +
            "?branch=main&status=success&per_page=1",
          "https://api.github.com/repos/jonapoul/aktual/compare/abcd1234...$NIGHTLY_SHA?per_page=1",
        ),
      )
  }

  @Test
  fun `Nothing when the nightly is the installed commit`() = runTest {
    mockEngine += { respondJson(runsJson(sha = "abcd1234ffffffff")) }
    val updates = buildUpdates(enabled = true)

    updates.latest.test {
      assertThat(awaitItem()).isNull()
      awaitRequests(1)
      expectNoEvents()
    }
    assertThat(mockEngine.requestHistory.size).isEqualTo(1)
  }

  @Test
  fun `Nothing when the installed build is newer`() = runTest {
    mockEngine += { respondJson(runsJson(sha = NIGHTLY_SHA)) }
    mockEngine += { respondJson(compareJson(status = "behind", aheadBy = 0)) }
    val updates = buildUpdates(enabled = true)

    updates.latest.test {
      assertThat(awaitItem()).isNull()
      awaitRequests(2)
      expectNoEvents()
    }
  }

  @Test
  fun `Nothing when the request fails`() = runTest {
    mockEngine += { respondError(NotFound) }
    val updates = buildUpdates(enabled = true)

    updates.latest.test {
      assertThat(awaitItem()).isNull()
      awaitRequests(1)
      expectNoEvents()
    }
  }

  @Test
  fun `No requests while disabled`() = runTest {
    val updates = buildUpdates(enabled = false)

    updates.latest.test {
      assertThat(awaitItem()).isNull()
      advanceUntilIdle()
      expectNoEvents()
    }
    assertThat(mockEngine.requestHistory).isEmpty()
  }

  @Test
  fun `No requests on debug builds`() = runTest {
    val updates =
      buildUpdates(enabled = true, buildConfig = TestBuildConfig.copy(buildType = "debug"))

    updates.latest.test {
      assertThat(awaitItem()).isNull()
      advanceUntilIdle()
      expectNoEvents()
    }
    assertThat(updates.isSupported).isEqualTo(false)
    assertThat(mockEngine.requestHistory).isEmpty()
  }

  // The mock engine serves requests on its own threads, outside the test scheduler, so this waits
  // in real time
  @Suppress("InjectDispatcher")
  private suspend fun awaitRequests(count: Int) =
    withContext(Dispatchers.Default) {
      withTimeout(5.seconds) {
        while (mockEngine.requestHistory.size < count) delay(10.milliseconds)
      }
    }

  private fun TestScope.buildUpdates(
    enabled: Boolean,
    buildConfig: BuildConfig = TestBuildConfig,
  ) =
    NightlyUpdatesImpl(
      githubApi = GithubApiImpl(testHttpClient(mockEngine, GithubJson)),
      buildConfig = buildConfig,
      contexts = TestCoroutineContexts(unconfinedDispatcher),
      installer = FakeApkInstaller(),
      preferences = FakeAppPreferences(checkForNightlyUpdates = enabled),
      scope = AppCoroutineScope(backgroundScope),
    )

  @Language("JSON")
  private fun runsJson(sha: String) =
    """
    {
      "total_count": 1,
      "workflow_runs": [
        {
          "id": 123,
          "head_sha": "$sha",
          "head_commit": { "id": "$sha", "timestamp": "2026-11-18T21:23:52Z" }
        }
      ]
    }
    """
      .trimIndent()

  @Language("JSON")
  private fun compareJson(status: String, aheadBy: Int) =
    """{ "status": "$status", "ahead_by": $aheadBy, "behind_by": 0 }"""

  private companion object {
    const val NIGHTLY_SHA = "dbc10893b6f847e502018520265fb62a327f6d3f"
  }
}
