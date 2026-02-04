package aktual.about.data

import aktual.test.TestBuildConfig
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.matchesPredicate
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
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
    mockEngine += { respondJson(NEW_RELEASE) }

    // When
    githubRepository.fetchLatestRelease()
    val request = mockEngine.requestHistory.last()

    // Then
    val expected = "https://api.github.com/repos/jonapoul/aktual/releases/latest?per_page=1"
    assertThat(request.url.toString()).isEqualTo(expected)
    assertThat(request.url.protocol).isEqualTo(URLProtocol.HTTPS)
  }

  @Test
  fun `Update available from three returned`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NEW_RELEASE) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(TestBuildConfig.versionName).isEqualTo("1.2.3")
    assertThat(state).isDataClassEqualTo(
      LatestReleaseState.UpdateAvailable(
        release = GithubRelease(
          versionName = "v2.3.4",
          publishedAt = Instant.parse("2024-03-30T09:57:02Z"),
          htmlUrl = "https://github.com/jonapoul/aktual/releases/tag/2.3.4",
          tagName = "2.3.4",
        ),
      ),
    )
  }

  @Test
  fun `No new updates`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(NO_NEW_UPDATE) }

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
    mockEngine += { respondJson(NOT_FOUND, HttpStatusCode.NotFound) }

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
    assertThat(state)
      .isInstanceOf<LatestReleaseState.Failure>()
      .matchesPredicate {
        it.errorMessage.contains("Failed decoding JSON")
      }
  }

  @Test
  fun `Network failure`() = runTest {
    // Given
    buildRepo()
    mockEngine += { throw IOException("Network broke!") }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state)
      .isInstanceOf<LatestReleaseState.Failure>()
      .prop(LatestReleaseState.Failure::errorMessage)
      .contains("IO failure")
  }

  @Test
  fun `HTTP failure`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondError(HttpStatusCode.MethodNotAllowed) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state)
      .isInstanceOf<LatestReleaseState.Failure>()
      .prop(LatestReleaseState.Failure::errorMessage)
      .contains("HTTP error 405")
  }

  private fun TestScope.buildRepo() {
    mockEngine = emptyMockEngine()
    githubRepository = GithubRepository(
      contexts = TestCoroutineContexts(standardDispatcher),
      buildConfig = TestBuildConfig,
      githubApi = GithubApiImpl(client = testHttpClient(mockEngine, GithubJson)),
    )
  }
}
