/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.about.ui

import actual.about.data.GithubRepository
import actual.about.data.LatestReleaseState
import actual.about.ui.info.InfoNavigator
import actual.about.ui.info.InfoScreen
import actual.about.ui.info.Tags
import actual.about.vm.AboutViewModel
import actual.core.model.ActualVersionsStateHolder
import actual.core.model.UrlOpener
import actual.test.TestBuildConfig
import actual.test.onDisplayedNodeWithTag
import actual.test.printTreeToLog
import actual.test.runTest
import actual.test.setAndroidThemedContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import github.api.model.GithubRelease
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
class AboutScreenTest {
  @get:Rule val composeRule = createComposeRule()

  // real
  private lateinit var viewModel: AboutViewModel
  private lateinit var actualVersionsStateHolder: ActualVersionsStateHolder

  // mock
  private lateinit var navigator: InfoNavigator
  private lateinit var urlOpener: UrlOpener
  private lateinit var githubRepository: GithubRepository

  @BeforeTest
  fun before() {
    navigator = mockk(relaxed = true)
    urlOpener = mockk(relaxed = true)
    githubRepository = mockk(relaxed = true)
    actualVersionsStateHolder = ActualVersionsStateHolder(BUILD_CONFIG)

    viewModel = AboutViewModel(
      buildConfig = BUILD_CONFIG,
      githubRepository = githubRepository,
      urlOpener = urlOpener,
      actualVersionsStateHolder = actualVersionsStateHolder,
    )
  }

  @Test
  fun clickingReportButtonOpensUrl() = composeRule.runTest {
    // given
    setAndroidThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    onDisplayedNodeWithTag(Tags.ReportButton).performClick()

    // then
    runOnIdle {
      verify(exactly = 1) { urlOpener(url = "${BUILD_CONFIG.repoUrl}/issues/new") }
    }
  }

  @Test
  fun clickingSourceCodeButtonOpensUrl() = composeRule.runTest {
    // given
    setAndroidThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    onDisplayedNodeWithTag(Tags.SourceCodeButton).performClick()

    // then
    runOnIdle {
      verify(exactly = 1) { urlOpener(BUILD_CONFIG.repoUrl) }
    }
  }

  @Test
  fun clickingLicensesButtonNavigates() = composeRule.runTest {
    // given
    setAndroidThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    onDisplayedNodeWithTag(Tags.LicensesButton).performClick()

    // then
    runOnIdle {
      verify(exactly = 1) { navigator.toLicenses() }
    }
  }

  @Test
  fun successfullyCheckForUpdatesThenOpenReleaseUrl() = composeRule.runTest {
    // given
    coEvery { githubRepository.fetchLatestRelease() } answers { LatestReleaseState.UpdateAvailable(GITHUB_RELEASE) }
    setAndroidThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view and click the button
    onDisplayedNodeWithTag(Tags.CheckUpdatesButton).performClick()
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
    verify(exactly = 1) { urlOpener(RELEASE_URL) }

    // and the dialog closes
    onNodeWithTag(Tags.UpdateFoundDialog).assertIsNotDisplayed()
  }

  @Test
  fun serverVersionUnknown() = composeRule.runTest {
    // given
    actualVersionsStateHolder.update { it.copy(server = null) }
    setAndroidThemedContent { InfoScreen(navigator, viewModel) }

    // when
    scrollTo(Tags.ServerVersionText)

    // then
    onNodeWithTag(Tags.ServerVersionText, useUnmergedTree = true)
      .onChildren()
      .filterToOne(hasTestTag(Tags.BuildStateItemValue))
      .assertTextEquals("Unknown")
  }

  @Test
  fun serverVersionKnown() = composeRule.runTest {
    // given
    actualVersionsStateHolder.update { it.copy(server = "v7.8.9") }
    setAndroidThemedContent { InfoScreen(navigator, viewModel) }

    // when
    scrollTo(Tags.ServerVersionText)

    // then
    onNodeWithTag(Tags.ServerVersionText, useUnmergedTree = true)
      .onChildren()
      .filterToOne(hasTestTag(Tags.BuildStateItemValue))
      .assertTextEquals("v7.8.9")
  }

  private fun ComposeContentTestRule.scrollTo(tag: String) {
    onDisplayedNodeWithTag(Tags.InfoScreenContent).performScrollToNode(hasTestTag(tag))
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
