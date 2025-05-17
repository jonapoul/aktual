package actual.about.info.ui

import actual.core.colorscheme.ColorSchemeType
import actual.core.ui.ActualTheme
import actual.test.ActualTestActivity
import actual.test.TestBuildConfig
import actual.test.runTest
import alakazam.kotlin.core.BuildConfig
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.net.toUri
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import github.api.client.GithubApi
import kotlinx.coroutines.Dispatchers
import org.hamcrest.Matchers
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@HiltAndroidTest
class AboutScreenTest {
  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1)
  val composeRule = createAndroidComposeRule<ActualTestActivity>()

  @BindValue
  lateinit var buildConfig: BuildConfig

  @BindValue
  lateinit var githubApiFactory: GithubApi.Factory

  @BindValue
  lateinit var coroutineContexts: CoroutineContexts

  @BeforeTest
  @Suppress("InjectDispatcher")
  fun before() {
    buildConfig = TestBuildConfig
    githubApiFactory = GithubApi.Factory { TestGithubApi() }
    coroutineContexts = TestCoroutineContexts(Dispatchers.Unconfined)

    hiltRule.inject()
    Intents.init()
  }

  @AfterTest
  fun after() {
    Intents.release()
  }

  @Test
  fun clickingReportIssuesLaunchesBrowser() = composeRule.runTest {
    // Given
    setContent()

    // When
    onNodeWithTag(Tags.ReportButton).performClick()

    // Then
    val expectedIntent = Matchers.allOf(
      hasAction(Intent.ACTION_VIEW),
      hasData("${TestBuildConfig.repoUrl}/issues/new".toUri()),
    )
    Intents.intended(expectedIntent)
  }

  private fun ComposeContentTestRule.setContent() {
    setContent {
      val navController = TestNavHostController(LocalContext.current)
      navController.navigatorProvider.addNavigator(ComposeNavigator())
      ActualTheme(ColorSchemeType.Light) { InfoScreen(navController) }
    }
  }
}
