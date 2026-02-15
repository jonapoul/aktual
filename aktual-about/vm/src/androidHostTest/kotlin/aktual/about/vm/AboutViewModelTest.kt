package aktual.about.vm

import aktual.about.data.GithubRelease
import aktual.about.data.GithubRepository
import aktual.about.data.LatestReleaseState
import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.UrlOpener
import aktual.test.TestBuildConfig
import aktual.test.TestInstant
import aktual.test.assertThatNextEmissionIsEqualTo
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutViewModelTest {
  // real
  private lateinit var viewModel: AboutViewModel
  private lateinit var aktualVersionsStateHolder: AktualVersionsStateHolder

  // mock
  private lateinit var repository: GithubRepository
  private lateinit var urlOpener: UrlOpener

  @Before
  fun before() {
    repository = mockk()
    urlOpener = mockk(relaxed = true)
    aktualVersionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)

    viewModel =
      AboutViewModel(
        buildConfig = TestBuildConfig,
        githubRepository = repository,
        urlOpener = urlOpener,
        aktualVersionsStateHolder = aktualVersionsStateHolder,
      )
  }

  @Test
  fun `Successfully fetch new release`() = runTest {
    // Given the repo returns a new update
    val url = "www.website.com/whatever"
    val version = "2.3.4"
    val model =
      GithubRelease(
        versionName = version,
        publishedAt = TestInstant,
        htmlUrl = url,
        tagName = "v1.2.3",
      )
    coEvery { repository.fetchLatestRelease() } returns LatestReleaseState.UpdateAvailable(model)

    viewModel.checkUpdatesState.test {
      assertThatNextEmissionIsEqualTo(CheckUpdatesState.Inactive)

      // When we fetch updates
      viewModel.fetchLatestRelease()
      assertThatNextEmissionIsEqualTo(CheckUpdatesState.Checking)

      // Then an update is returned
      assertThatNextEmissionIsEqualTo(CheckUpdatesState.UpdateFound(version, url))

      // When we cancel the dialog
      viewModel.cancelUpdateCheck()

      // Then the dialog is dismissed
      assertThatNextEmissionIsEqualTo(CheckUpdatesState.Inactive)
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Open issues page`() = runTest {
    // When
    viewModel.reportIssues()

    // Then
    verify(exactly = 1) { urlOpener("https://github.com/jonapoul/aktual/issues/new") }
    confirmVerified(urlOpener)
  }

  @Test
  fun `Open repo page`() = runTest {
    // When
    viewModel.openRepo()

    // Then
    verify(exactly = 1) { urlOpener("https://github.com/jonapoul/aktual") }
    confirmVerified(urlOpener)
  }
}
