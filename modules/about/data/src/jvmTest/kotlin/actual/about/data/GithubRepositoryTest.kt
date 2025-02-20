package actual.about.data

import actual.core.config.BuildConfig
import actual.test.MockWebServerRule
import actual.test.TestBuildConfig
import actual.test.getResourceAsText
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import github.api.client.GithubApi
import github.api.client.GithubJson
import github.api.model.GithubRelease
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GithubRepositoryTest {
  @get:Rule
  val webServerRule = MockWebServerRule()

  private lateinit var githubRepository: GithubRepository
  private lateinit var githubApi: GithubApi
  private lateinit var buildConfig: BuildConfig

  @Before
  fun before() {
    buildConfig = TestBuildConfig
    githubApi = webServerRule.buildApi(json = GithubJson)
  }

  @Test
  fun `Update available from three returned`() = runTest {
    // Given
    buildRepo()
    val threeReleasesJson = getResourceAsText(filename = "new-release.json")
    webServerRule.enqueue(threeReleasesJson)

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(expected = "1.2.3", actual = buildConfig.versionName)
    assertEquals(
      actual = state,
      expected = LatestReleaseState.UpdateAvailable(
        release = GithubRelease(
          versionName = "v2.3.4",
          publishedAt = Instant.parse("2024-03-30T09:57:02Z"),
          htmlUrl = "https://github.com/jonapoul/actual-android/releases/tag/2.3.4",
        ),
      ),
    )
  }

  @Test
  fun `No new updates`() = runTest {
    // Given
    buildRepo()
    val threeReleasesJson = getResourceAsText(filename = "no-new-update.json")
    webServerRule.enqueue(threeReleasesJson)

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(expected = "1.2.3", actual = buildConfig.versionName)
    assertEquals(actual = state, expected = LatestReleaseState.NoNewUpdate)
  }

  @Test
  fun `No releases`() = runTest {
    // Given
    buildRepo()
    val emptyArrayResponse = "[]"
    webServerRule.enqueue(emptyArrayResponse)

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(actual = state, expected = LatestReleaseState.NoReleases)
  }

  @Test
  fun `Private repo`() = runTest {
    // Given
    buildRepo()
    val privateRepoJson = getResourceAsText(filename = "not-found.json")
    webServerRule.enqueue(privateRepoJson)

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertEquals(actual = state, expected = LatestReleaseState.PrivateRepo)
  }

  @Test
  fun `Invalid JSON response`() = runTest {
    // Given
    buildRepo()
    val jsonResponse = """
      {
        "foo" : "bar"
      }
    """.trimIndent()
    webServerRule.enqueue(jsonResponse)

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
    githubApi = GithubApi { throw IOException("Network broke!") }
    buildRepo()

    // When
    val state = githubRepository.fetchLatestRelease()

    // Then
    assertIs<LatestReleaseState.Failure>(state)
    assertTrue(state.errorMessage.contains("IO failure"), state.errorMessage)
  }

  private fun TestScope.buildRepo() {
    githubRepository = GithubRepository(
      contexts = TestCoroutineContexts(standardDispatcher),
      api = githubApi,
      buildConfig = buildConfig,
    )
  }
}
