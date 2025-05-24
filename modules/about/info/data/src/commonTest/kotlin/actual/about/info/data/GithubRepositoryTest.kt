package actual.about.info.data

import actual.test.TestBuildConfig
import actual.test.emptyMockEngine
import actual.test.plusAssign
import actual.test.respondJson
import actual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import github.api.client.GithubApi
import github.api.client.GithubJson
import github.api.model.GithubRelease
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GithubRepositoryTest {
  private lateinit var githubRepository: GithubRepository
  private lateinit var mockEngine: MockEngine

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Request is structured as expected`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NewRelease) }

    // When
    githubRepository.fetchLatestRelease()
    val request = mockEngine.requestHistory.last()

    // Then
    val expected = "https://api.github.com/repos/jonapoul/actual-android/releases/latest?per_page=1"
    assertEquals(expected = expected, actual = request.url.toString())
    assertEquals(expected = URLProtocol.HTTPS, actual = request.url.protocol)
  }

  @Test
  fun `Update available from three returned`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NewRelease) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(expected = "1.2.3", actual = TestBuildConfig.versionName)
    assertEquals(
      actual = state,
      expected = LatestReleaseState.UpdateAvailable(
        release = GithubRelease(
          versionName = "v2.3.4",
          publishedAt = Instant.parse("2024-03-30T09:57:02Z"),
          htmlUrl = "https://github.com/jonapoul/actual-android/releases/tag/2.3.4",
          tagName = "2.3.4",
        ),
      ),
    )
  }

  @Test
  fun `No new updates`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NoNewUpdate) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(expected = "1.2.3", actual = TestBuildConfig.versionName)
    assertEquals(actual = state, expected = LatestReleaseState.NoNewUpdate)
  }

  @Test
  fun `No releases`() = runTest {
    // Given
    val emptyArrayResponse = "[]"
    buildRepo()
    mockEngine += { respondJson(emptyArrayResponse) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(actual = state, expected = LatestReleaseState.NoReleases)
  }

  @Test
  fun `Private repo`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NotFound, HttpStatusCode.NotFound) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(actual = state, expected = LatestReleaseState.PrivateRepo)
  }

  @Test
  fun `Invalid JSON response`() = runTest {
    // Given
    buildRepo()
    val jsonResponse = """{"foo" : "bar"}"""
    mockEngine += { respondJson(jsonResponse) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertIs<LatestReleaseState.Failure>(state)
    assertTrue(state.errorMessage.contains("Failed decoding JSON"), state.errorMessage)
  }

  @Test
  fun `Network failure`() = runTest {
    // Given
    buildRepo()
    mockEngine += { throw IOException("Network broke!") }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertIs<LatestReleaseState.Failure>(state)
    assertTrue(state.errorMessage.contains("IO failure"), state.errorMessage)
  }

  @Test
  fun `HTTP failure`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondError(HttpStatusCode.MethodNotAllowed) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertIs<LatestReleaseState.Failure>(state)
    assertTrue(state.errorMessage.contains("HTTP error 405"), state.errorMessage)
  }

  private fun TestScope.buildRepo() {
    mockEngine = emptyMockEngine()
    val githubApi = GithubApi(client = testHttpClient(mockEngine, GithubJson))
    githubRepository = GithubRepository(
      contexts = TestCoroutineContexts(standardDispatcher),
      buildConfig = TestBuildConfig,
      apiFactory = GithubApi.Factory { githubApi },
    )
  }
}
