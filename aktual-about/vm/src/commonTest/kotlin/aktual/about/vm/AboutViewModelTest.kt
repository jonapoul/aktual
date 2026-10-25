package aktual.about.vm

import aktual.about.data.GithubRepository
import aktual.about.data.LatestReleaseState.UpdateAvailable
import aktual.about.vm.CheckUpdatesState.UpdateFound
import aktual.core.model.AktualVersionsStateHolder
import aktual.test.TestBuildConfig
import aktual.test.TestInstant
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import github.api.model.GithubRelease
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class AboutViewModelTest {
  // real
  private lateinit var viewModel: AboutViewModel
  private lateinit var aktualVersionsStateHolder: AktualVersionsStateHolder

  // mock
  private lateinit var repository: GithubRepository

  private fun TestScope.before() {
    repository = mockk()
    aktualVersionsStateHolder = AktualVersionsStateHolder(TestBuildConfig)

    viewModel =
      AboutViewModel(
        savedState = SavedStateHandle(),
        buildConfig = TestBuildConfig,
        githubRepository = repository,
        aktualVersionsStateHolder = aktualVersionsStateHolder,
        viewModelContext = viewModelContext,
      )
  }

  @Test
  fun `Successfully fetch new release`() = runTest {
    before()
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
    coEvery { repository.fetchLatestRelease() } returns UpdateAvailable(model)

    viewModel.checkUpdatesState.test {
      assertThatNextEmissionIsEqualTo(Inactive)

      // When we fetch updates
      viewModel.fetchLatestRelease()
      assertThatNextEmissionIsEqualTo(Checking)

      // Then an update is returned
      assertThatNextEmissionIsEqualTo(UpdateFound(version, url))

      // When we cancel the dialog
      viewModel.cancelUpdateCheck()

      // Then the dialog is dismissed
      assertThatNextEmissionIsEqualTo(Inactive)
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
