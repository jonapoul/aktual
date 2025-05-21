package actual.about.info.ui

import actual.about.info.data.GithubRepository
import actual.about.info.vm.InfoViewModel
import actual.test.TestBuildConfig
import actual.test.setThemedContent
import alakazam.android.core.UrlOpener
import alakazam.test.core.MainDispatcherRule
import alakazam.test.core.TestCoroutineContexts
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import github.api.client.GithubApi
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class InfoScreenTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()
  @get:Rule val composeRule = createComposeRule()

  // real
  private lateinit var viewModel: InfoViewModel
  private lateinit var githubRepository: GithubRepository

  // mock
  private lateinit var navigator: InfoNavigator
  private lateinit var urlOpener: UrlOpener
  private lateinit var githubApi: GithubApi

  @BeforeTest
  fun before() {
    navigator = mockk(relaxed = true)
    urlOpener = mockk(relaxed = true)
    githubApi = mockk(relaxed = true)

    githubRepository = GithubRepository(
      contexts = TestCoroutineContexts(mainDispatcherRule.dispatcher),
      apiFactory = GithubApi.Factory { githubApi },
      buildConfig = TestBuildConfig,
    )

    viewModel = InfoViewModel(
      buildConfig = TestBuildConfig,
      githubRepository = githubRepository,
      urlOpener = urlOpener,
    )
  }

  @Test
  fun clickingReportButtonOpensUrl() = composeRule.run {
    // given
    setThemedContent { InfoScreen(navigator, viewModel) }

    // when we scroll the button into view
    onNode(hasTestTag(Tags.InfoScreenContent) and hasScrollAction())
      .assertExists()
      .assertIsDisplayed()
      .performScrollToNode(hasTestTag(Tags.ReportButton))

    // and click the button
    onNodeWithTag(Tags.ReportButton)
      .assertExists()
      .assertIsEnabled()
      .assertIsDisplayed()
      .performClick()

    // then
    runOnIdle {
      verify(exactly = 1) { urlOpener.openUrl(url = "${TestBuildConfig.repoUrl}/issues/new") }
    }
  }
}
