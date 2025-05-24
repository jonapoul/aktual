package actual.about.info.vm

import actual.about.info.data.GithubRepository
import actual.about.info.data.LatestReleaseState
import actual.core.model.ActualVersionsStateHolder
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
import kotlin.test.BeforeTest
import kotlin.test.Test

class InfoViewModelTest {
  // real
  private lateinit var viewModel: InfoViewModel
  private lateinit var actualVersionsStateHolder: ActualVersionsStateHolder

  // mock
  private lateinit var repository: GithubRepository
  private lateinit var urlOpener: UrlOpener

  @BeforeTest
  fun before() {
    repository = mockk()
    urlOpener = mockk(relaxed = true)
    actualVersionsStateHolder = ActualVersionsStateHolder(TestBuildConfig)

    viewModel = InfoViewModel(
      buildConfig = TestBuildConfig,
      githubRepository = repository,
      urlOpener = urlOpener,
      actualVersionsStateHolder = actualVersionsStateHolder,
    )
  }

  @Test
  fun `Successfully fetch new release`() = runTest {
    // Given the repo returns a new update
    val url = "www.website.com/whatever"
    val version = "2.3.4"
    val model = GithubRelease(version, TestInstant, url, tagName = "v1.2.3")
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
