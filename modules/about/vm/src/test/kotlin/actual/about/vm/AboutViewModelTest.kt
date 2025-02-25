package actual.about.vm

import actual.about.data.GithubRepository
import actual.about.data.LatestReleaseState
import actual.test.TestBuildConfig
import actual.test.TestInstant
import actual.test.assertEmitted
import alakazam.android.core.UrlOpener
import app.cash.turbine.test
import github.api.model.GithubRelease
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutViewModelTest {
  // real
  private lateinit var viewModel: AboutViewModel

  // mock
  private lateinit var repository: GithubRepository
  private lateinit var urlOpener: UrlOpener

  @Before
  fun before() {
    repository = mockk()
    urlOpener = mockk(relaxed = true)
    viewModel = AboutViewModel(
      buildConfig = TestBuildConfig,
      githubRepository = repository,
      urlOpener = urlOpener,
    )
  }

  @Test
  fun `Successfully fetch new release`() = runTest {
    // Given the repo returns a new update
    val url = "www.website.com/whatever"
    val version = "2.3.4"
    val model = GithubRelease(version, TestInstant, url)
    coEvery { repository.fetchLatestRelease() } returns LatestReleaseState.UpdateAvailable(model)

    viewModel.checkUpdatesState.test {
      assertEmitted(CheckUpdatesState.Inactive)

      // When we fetch updates
      viewModel.fetchLatestRelease()
      assertEmitted(CheckUpdatesState.Checking)

      // Then an update is returned
      assertEmitted(CheckUpdatesState.UpdateFound(version, url))

      // When we cancel the dialog
      viewModel.cancelUpdateCheck()

      // Then the dialog is dismissed
      assertEmitted(CheckUpdatesState.Inactive)
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Open issues page`() = runTest {
    // When
    viewModel.reportIssues()

    // Then
    verify(exactly = 1) { urlOpener.openUrl("https://github.com/jonapoul/actual-android/issues/new") }
    confirmVerified(urlOpener)
  }

  @Test
  fun `Open repo page`() = runTest {
    // When
    viewModel.openRepo()

    // Then
    verify(exactly = 1) { urlOpener.openUrl("https://github.com/jonapoul/actual-android") }
    confirmVerified(urlOpener)
  }
}
