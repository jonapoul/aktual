package actual.about.info.ui

import actual.about.info.data.GithubRepository
import actual.about.info.data.LatestReleaseState
import actual.about.info.vm.InfoViewModel
import actual.test.TestBuildConfig
import actual.test.onDisplayedNodeWithTag
import actual.test.printTreeToLog
import actual.test.runTest
import actual.test.setThemedContent
import alakazam.android.core.UrlOpener
import alakazam.test.core.MainDispatcherRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import github.api.model.GithubRelease
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class InfoScreenTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  @get:Rule val composeRule = createComposeRule()

  // real
  private lateinit var viewModel: InfoViewModel

  // mock
  private lateinit var navigator: InfoNavigator
  private lateinit var urlOpener: UrlOpener
  private lateinit var githubRepository: GithubRepository

  @BeforeTest
  fun before() {
    ShadowLog.stream = System.out

    navigator = mockk(relaxed = true)
    urlOpener = mockk(relaxed = true)
    githubRepository = mockk(relaxed = true)

    viewModel = InfoViewModel(
      buildConfig = BUILD_CONFIG,
      githubRepository = githubRepository,
      urlOpener = urlOpener,
    )
  }

  @Test
  fun clickingReportButtonOpensUrl() = composeRule.runTest {
    // given
    setThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    scrollToAndClick(Tags.ReportButton)

    // then
    runOnIdle {
      verify(exactly = 1) { urlOpener.openUrl(url = "${BUILD_CONFIG.repoUrl}/issues/new") }
    }
  }

  @Test
  fun clickingSourceCodeButtonOpensUrl() = composeRule.runTest {
    // given
    setThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    scrollToAndClick(Tags.SourceCodeButton)

    // then
    runOnIdle {
      verify(exactly = 1) { urlOpener.openUrl(BUILD_CONFIG.repoUrl) }
    }
  }

  @Test
  fun clickingLicensesButtonNavigates() = composeRule.runTest {
    // given
    setThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    scrollToAndClick(Tags.LicensesButton)

    // then
    runOnIdle {
      verify(exactly = 1) { navigator.toLicenses() }
    }
  }

  @Test
  fun successfullyCheckForUpdatesThenOpenReleaseUrl() = composeRule.runTest {
    // given
    coEvery { githubRepository.fetchLatestRelease() } answers { LatestReleaseState.UpdateAvailable(GITHUB_RELEASE) }
    setThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    scrollToAndClick(Tags.CheckUpdatesButton)
    waitForIdle()

    printTreeToLog(root = onNodeWithTag(Tags.UpdateFoundDialog))

    // then
    onDisplayedNodeWithTag(Tags.UpdateAvailableCurrentVersion)
      .assertTextEquals("1.2.3")
    onNodeWithTag(Tags.UpdateAvailableNewVersion)
      .assertIsDisplayed()
      .assertTextEquals("2.3.4")

    // when we click the download button
    onDisplayedNodeWithTag(Tags.UpdateAvailableDownloadButton).performClick()

    // then the release page is opened
    waitForIdle()
    verify(exactly = 1) { urlOpener.openUrl(RELEASE_URL) }

    // and the dialog closes
    onNodeWithTag(Tags.UpdateFoundDialog).assertIsNotDisplayed()
  }

  private fun ComposeContentTestRule.scrollToAndClick(tag: String) {
    onDisplayedNodeWithTag(Tags.InfoScreenContent).performScrollToNode(hasTestTag(tag))
    onDisplayedNodeWithTag(tag).performClick()
  }

  private companion object {
    val BUILD_CONFIG = TestBuildConfig.copy(
      versionName = "1.2.3",
      versionCode = 123,
    )

    const val RELEASE_URL = "https://url.of.website.com/release"
    val GITHUB_RELEASE = GithubRelease(
      versionName = "2.3.4",
      publishedAt = Instant.fromEpochMilliseconds(123456789L),
      htmlUrl = RELEASE_URL,
      tagName = "v2.3.4",
    )
  }
}
