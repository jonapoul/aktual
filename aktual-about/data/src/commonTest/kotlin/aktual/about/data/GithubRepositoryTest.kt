package aktual.about.data

import aktual.core.model.BuildConfig
import aktual.test.GithubResponses
import aktual.test.TestBuildConfig
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.matchesPredicate
import assertk.assertions.prop
import github.api.client.GithubApiImpl
import github.api.client.GithubJson
import github.api.model.GithubRelease
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

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
    mockEngine += { respondJson(GithubResponses.LIST_RELEASES_ACTUAL_200) }

    // When
    githubRepository.fetchLatestRelease()
    val request = mockEngine.requestHistory.last()

    // Then
    val expected = "https://api.github.com/repos/jonapoul/aktual/releases?per_page=20"
    assertThat(request.url.toString()).isEqualTo(expected)
    assertThat(request.url.protocol).isEqualTo(HTTPS)
  }

  @Test
  fun `Update available from three returned`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(GithubResponses.LIST_RELEASES_ACTUAL_200) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(TestBuildConfig.versionName).isEqualTo("1.2.3")
    assertThat(state).isEqualTo(LatestReleaseState.UpdateAvailable(STABLE))
  }

  @Test
  fun `Pre-releases and drafts are skipped`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(GithubResponses.LIST_RELEASES_WITH_PRERELEASE_200) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state).isEqualTo(LatestReleaseState.UpdateAvailable(STABLE))
  }

  @Test
  fun `Pre-release builds are offered pre-releases`() = runTest {
    // Given
    buildRepo(TestBuildConfig.copy(isPrerelease = true))
    mockEngine += { respondJson(GithubResponses.LIST_RELEASES_WITH_PRERELEASE_200) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state)
      .isEqualTo(
        LatestReleaseState.UpdateAvailable(
          GithubRelease(
            versionName = "v2.4.0-beta",
            publishedAt = Instant.parse("2024-04-10T09:00:00Z"),
            htmlUrl = "https://github.com/jonapoul/aktual/releases/tag/2.4.0-beta",
            tagName = "2.4.0-beta",
            isPrerelease = true,
          ),
        ),
      )
  }

  @Test
  fun `Only drafts means no releases`() = runTest {
    // Given
    val drafts =
      """[{"name": "v3.0.0", "published_at": null, "html_url": "x", "tag_name": "3.0.0", "draft": true}]"""
    buildRepo(TestBuildConfig.copy(isPrerelease = true))
    mockEngine += { respondJson(drafts) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state).isEqualTo(NoReleases)
  }

  @Test
  fun `No new updates`() = runTest {
    // Given
    buildRepo()
    mockEngine += { respondJson(GithubResponses.LIST_RELEASES_NO_NEW_RELEASE_200) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(TestBuildConfig.versionName).isEqualTo("1.2.3")
    assertThat(state).isEqualTo(NoNewUpdate)
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
    assertThat(state).isEqualTo(NoReleases)
  }

  @Test
  fun `Private repo`() = runTest {
    // Given
    buildRepo()
    mockEngine += {
      respondJson(GithubResponses.LIST_RELEASES_NOT_FOUND_404, NotFound)
    }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state).isEqualTo(PrivateRepo)
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
    assertThat(state).isInstanceOf<LatestReleaseState.Failure>().matchesPredicate {
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
    mockEngine += { respondError(MethodNotAllowed) }

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertThat(state)
      .isInstanceOf<LatestReleaseState.Failure>()
      .prop(LatestReleaseState.Failure::errorMessage)
      .contains("HTTP error 405")
  }

  private fun TestScope.buildRepo(buildConfig: BuildConfig = TestBuildConfig) {
    mockEngine = emptyMockEngine()
    githubRepository =
      GithubRepository(
        contexts = TestCoroutineContexts(standardDispatcher),
        buildConfig = buildConfig,
        githubApi = GithubApiImpl(client = testHttpClient(mockEngine, GithubJson)),
      )
  }

  private companion object {
    val STABLE =
      GithubRelease(
        versionName = "v2.3.4",
        publishedAt = Instant.parse("2024-03-30T09:57:02Z"),
        htmlUrl = "https://github.com/jonapoul/aktual/releases/tag/2.3.4",
        tagName = "2.3.4",
      )
  }
}
