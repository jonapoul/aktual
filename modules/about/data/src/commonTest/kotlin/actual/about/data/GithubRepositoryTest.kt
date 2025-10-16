package actual.about.data

import actual.test.TestBuildConfig
import actual.test.emptyMockEngine
import actual.test.respondJson
import actual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import assertk.assertThat
import assertk.assertions.isEqualTo
import github.api.client.GithubApi
import github.api.client.GithubJson
import github.api.model.GithubRelease
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class GithubRepositoryTest {
  private lateinit var githubRepository: GithubRepository
  private lateinit var mockEngine: MockEngine.Queue

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
    assertThat(request.url.toString()).isEqualTo(expected)
    assertThat(request.url.protocol).isEqualTo(URLProtocol.HTTPS)
  }

  @Test
  fun `Update available from three returned`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NewRelease) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(TestBuildConfig.versionName).isEqualTo("1.2.3")
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
    assertThat(TestBuildConfig.versionName).isEqualTo("1.2.3")
    assertThat(state).isEqualTo(LatestReleaseState.NoNewUpdate)
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
    assertThat(state).isEqualTo(LatestReleaseState.NoReleases)
  }

  @Test
  fun `Private repo`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NotFound, HttpStatusCode.NotFound) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state).isEqualTo(LatestReleaseState.PrivateRepo)
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
